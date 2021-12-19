package com.cyberark.controllers;

import com.cyberark.Application;
import com.cyberark.Util;
import com.cyberark.actions.ActionType;
import com.cyberark.actions.ViewNavigationAction;
import com.cyberark.components.ApiCallLogView;
import com.cyberark.components.MainForm;
import com.cyberark.event.*;
import com.cyberark.exceptions.ResourceAccessException;
import com.cyberark.models.*;
import com.cyberark.models.audit.AuditEvent;
import com.cyberark.models.table.DefaultResourceTableModel;
import com.cyberark.models.table.PolicyTableModel;
import com.cyberark.models.table.RoleTableModel;
import com.cyberark.models.table.SecretTableModel;
import com.cyberark.resource.ResourceServiceFactory;
import com.cyberark.resource.ResourcesService;
import com.cyberark.views.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.FileNotFoundException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.cyberark.Util.getViewType;

class ViewControllerImpl implements ViewController {
  private static final Logger logger = LogManager.getLogger(ViewControllerImpl.class);
  private final Map<ViewType, View> views = new HashMap<>();
  private ViewType currentViewType;

  ViewControllerImpl() {
    EventPublisher.getInstance().addListener(this::onResourceEvent);
    EventPublisher.getInstance().addApplicationEventListener(this::onApplicationEvent);
  }

  private void onApplicationEvent(ApplicationEvent e) {
    switch (e.getEventType()) {
      case ApiCall:
        logApiCall((ApiCallEvent) e);
        break;
      case Login:
        onLogin();
        break;
      case Logout:
        onLogout();
        break;
    }
  }

  private void onLogin() {
    try {
      logger.debug("Register viewController to change on view selection");
      getMainForm().setViewSelectedListener(this::setView);

      logger.debug("Set initial view to {}", ViewType.Policies);
      setView(ViewType.Policies);

      logger.debug("Show main form");
      getMainForm().setVisible(true);
    } catch (Exception ex) {
      JOptionPane.showMessageDialog(getMainForm(), ex.toString(), "Error",
          JOptionPane.ERROR_MESSAGE);
      logger.error(ex);
    }
  }

  private void onLogout() {
    logger.debug("Notify controller to clear all views");
    clearAllViews();

    logger.debug("Hide main form");
    getMainForm().setVisible(false);

    // Close all dialogs as the events from the inactivityListener are random
    // and might trigger while a modal dialog is open
    for (Window w : JDialog.getWindows()) {
      if (w instanceof JDialog) {
        w.setVisible(false);
        w.dispose();
      }
    }
  }

  private void logApiCall(ApiCallEvent e) {
    StringBuilder builder = new StringBuilder();

    builder
        .append(System.lineSeparator())
        .append("-".repeat(64))
        .append(
            String.format(
                "%n url: %s%n request method: %s%n headers: %s%n body:%s%n---%n response code: %s%n response:%n%s",
                e.getUrl(),
                e.getRequestMethod(),
                e.getHeaders(),
                e.getBody(),
                e.getResponseCode(),
                Util.prettyPrintJson(e.getResponse())
            )
        );

    logger.debug(builder);
    ApiCallLogView.getInstance().append(builder.toString());
  }

  private void onResourceEvent(ActionEvent e) {
    logger.debug("onResourceEvent: {}", e);

    ResourceEvent<? extends ResourceModel> resourceEvent =
        (e.getSource() instanceof ResourceEvent)
            ? (ResourceEvent<? extends ResourceModel>) e.getSource()
            : null;

    if (currentViewType != ViewType.Dashboard) {
      if (resourceEvent == null && e.getID() == Events.NEW_ITEM) {
        reloadView();
      } else if (resourceEvent != null) {
        reloadViewIfNewResourceInCurrentView(resourceEvent);
      }
    }
  }

  private void reloadViewIfNewResourceInCurrentView(ResourceEvent<? extends ResourceModel> event) {
    ResourceIdentifier identifier = event.getResource().getIdentifier();
    ResourceType resourceType = identifier.getType();

    if (Util.getViewType(resourceType) == currentViewType) {
      reloadView();
    }
  }

  @Override
  public View getView(ViewType viewType) {
    logger.trace("getView enter::viewType: {}", viewType);
    View view = null;

    try {
      logger.debug("computeIfAbsent viewType: {}", viewType);
      views.computeIfAbsent(viewType, this::createView);

      view = views.get(viewType);

      logger.debug("set view model");
      setViewModel(viewType);
    } catch (Exception e) {
      logger.error(e);
      e.printStackTrace();
      ErrorView.showErrorMessage(e);
    }

    logger.trace("getView exit:: return {}", views.get(viewType));
    return view;
  }

  private ResourcesService getResourceService() {
    return ResourceServiceFactory.getInstance().getResourcesService();
  }

  private View createView(ViewType type) {
    View view = null;
    ResourceView resourceView = null;

    switch (type) {
      case Dashboard:
        view = new DashboardView(this::actionPerformed);
        break;
      case Policies:
        resourceView = new PoliciesView();
        break;
      case Secrets:
        resourceView = new SecretsView();
        break;
      case Users:
      case Hosts:
        resourceView = new RolesView(type);
        break;
      default:
        resourceView = new CommonResourceView(type);
        break;
    }

    if (type != ViewType.Dashboard) { // no operation on dashboard
      resourceView.setSelectionListener(r -> toggleSelectionBasedAction(r != null));
      resourceView.setTableRowDoubleClickedEventListener(this::fireEditActionPerformed);
    }

    return (type == ViewType.Dashboard ? view : resourceView);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    switch (e.getID()) {
      case Events.SERACH_RESOURCE:
        handleSearchEvent(e);
        break;
      case Events.NAVIGATE_TO_VIEW:
        if (e.getSource() instanceof ViewNavigationAction) {
          setView(getViewType(((ViewNavigationAction) e.getSource()).getResourceType()));
        }
        break;
      case Events.HELP:
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
          try {
            Desktop.getDesktop().browse(
                new URI(Application.getInstance().getSettingsProperty(e.getActionCommand()))
            );
          } catch (Exception ex) {
            ex.printStackTrace();
          }
        }
        break;
    }
  }

  private void handleSearchEvent(ActionEvent actionEvent) {
    logger.trace("handleSearchEvent::enter: {}", actionEvent);
    getCurrentView().applyFilter(actionEvent.getActionCommand());
    logger.trace("handleSearchEvent::exit: {}", actionEvent);
  }

  private View getCurrentView() {
    return views.get(currentViewType);
  }

  private void fireEditActionPerformed(DataModel model) {
    View view = views.get(getCurrentViewType());
    if (view instanceof ResourceView) {
      fireActionPerformed((ResourceView) view);
    }
  }

  private void fireActionPerformed(ResourceView view) {
    Action action = view.getAction(ActionType.EditItem);

    if (action != null) {
      action.actionPerformed(
          new ActionEvent(this, ActionEvent.ACTION_FIRST,
              ActionType.EditItem.toString()));
    }
  }

  private void toggleSelectionBasedAction(boolean enabled) {
    ((ResourceView) views.get(getCurrentViewType())).toggleSelectionBasedActions(enabled);
  }

  private void setViewModel(ViewType viewType) throws Exception {
    logger.trace("setViewModel enter:: {}", viewType);

    DefaultResourceTableModel<? extends ResourceModel> model;

    switch (viewType) {
      case Dashboard:
        views.get(viewType).setModel(
            new DashboardViewModel(getAuditEvents(), getResourceCountMap())
        );
        break;
      case Policies:
        getPoliciesView().setResourceTableModel(
            new PolicyTableModel(getResourceService().getPolicies())
        );
        break;
      case Hosts:
      case Users:
        getRolesView(viewType).setResourceTableModel(
          new RoleTableModel(
            getResourceService().getRoles(Util.getResourceType(viewType))
          )
        );
        break;
      case Secrets:
        getSecretsView().setResourceTableModel(
          getSecretsViewModel()
        );
        break;
      default:
        getResourceView(viewType).setResourceTableModel(
          new DefaultResourceTableModel<>(
            getResourceService().getResources(Util.getResourceType(viewType))
          )
        );
    }

    logger.trace("setViewModel exit::");

  }

  private CommonResourceView getResourceView(ViewType viewType) {
    return (CommonResourceView) views.get(viewType);
  }

  private PoliciesView getPoliciesView() {
    return (PoliciesView) views.get(ViewType.Policies);
  }

  private RolesView getRolesView(ViewType viewType) {
    return (RolesView) views.get(viewType);
  }

  private SecretsView getSecretsView() {
    return (SecretsView) views.get(ViewType.Secrets);
  }

  private List<AuditEvent> getAuditEvents() throws ResourceAccessException {
    logger.trace("getAuditEvents::enter::");

    List<AuditEvent> auditEvents = ResourceServiceFactory
        .getInstance()
        .getResourcesService()
        .getAuditEvents();

    logger.trace("getAuditEvents::exit:: return {} item(s)", auditEvents.size());
    return auditEvents;
  }

  private Map<ResourceType, Integer> getResourceCountMap() throws ResourceAccessException {
    logger.trace("getResourceCountMap::enter::");

    Map<ResourceType, Integer> map = new HashMap<>();

    for (ResourceType type : ResourceType.values()) {
      map.put(type, getResourceCount(type));
    }

    logger.trace("getResourceCountMap::exit:: return {}", map);
    return map;
  }

  private Integer getResourceCount(ResourceType type) throws ResourceAccessException {
    return getResourceService().getResourcesCount(type);
  }

  /**
   * Creates a secrets model and populates it with the latest secret version value
   *
   * @return ResourceTableModel
   * @throws ResourceAccessException In case of an error retrieving the secret
   */
  private SecretTableModel getSecretsViewModel() throws ResourceAccessException {
    logger.trace("getSecretsViewModel::enter::");

    List<SecretModel> secretModels = getResourceService().getVariables();
    SecretTableModel model = new SecretTableModel(secretModels);
    Exception[] errors = new Exception[1];

    // Populate the model with latest secret version
    secretModels.forEach(i -> {
      try {
        if (i.getSecrets().length > 0) {
          i.setSecret(getResourceService().getSecret(i).toCharArray());
        }
      } catch (ResourceAccessException e) {
        if (e.getCause() instanceof FileNotFoundException) {
          logger.warn("Secret with id: {}}, has no value\nError: {}}", i.getId(), e.getCause());
        } else {
          errors[0] = e;
          logger.error(e);
        }
      }
    });

    if (errors[0] != null) {
      throw new ResourceAccessException(errors[0]);
    }

    logger.trace("getSecretsViewModel::exit:: return: {}", model);
    return model;
  }

  @Override
  public void setStatusLabel(String msg) {
    getMainForm().setStatusLabel(msg);
  }

  @Override
  public void reloadView(ViewType view) {
    if (getMainForm().getComponentView().getType() == view) {
      try {
        getMainForm().clearSearchText();
        setViewModel(getMainForm().getComponentView().getType());
      } catch (Exception e) {
        e.printStackTrace();
        logger.error(e);
        ErrorView.showErrorMessage(e);
      }
    }
  }

  private ViewType getCurrentViewType() {
    Objects.requireNonNull(currentViewType);
    return currentViewType;
  }

  @Override
  public void setView(ViewType viewType) {
    logger.trace("setView enter::viewType {}", viewType);

    if (viewType == null) {
      throw new IllegalArgumentException("viewType cannot be null");
    }

    logger.debug("Set currentViewType: {}", viewType);
    currentViewType = viewType;

    logger.debug("Set main form view to: {}", viewType);
    getMainForm().setView(getView(currentViewType));

    logger.trace("setView exit::viewType {}", viewType);
  }

  @Override
  public void reloadView() {
    reloadView(getCurrentViewType());
  }

  @Override
  public void clearAllViews() {
    views.values().forEach(View::clearData);
  }

  private MainForm getMainForm() {
    return Application.getInstance().getMainForm();
  }
}
