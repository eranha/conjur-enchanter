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
import com.cyberark.models.table.*;
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
  private final Map<ViewType, View> views = new HashMap<>();
  private ViewType currentViewType;
  private static final Logger logger = LogManager.getLogger(ViewControllerImpl.class);

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
      if ( w instanceof JDialog) {
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
      } else {
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

      logger.debug("getViewModel viewType: {}", viewType);
      ViewModel model = getViewModel(viewType);

      logger.debug("setViewModel model: {}", model);
      view.setModel(model);
    } catch (Exception e) {
      showErrorDialog(e.toString());
      logger.error(e);
      e.printStackTrace();
    }

    logger.trace("getView exit:: return {}", views.get(viewType));
    return view;
  }

  private <T extends ResourceModel> List<T> getResources(ViewType viewType) throws ResourceAccessException {
    logger.trace("getResources({}) enter::", viewType);

    ResourceType type = Util.getResourceType(viewType);
    List<T> resources = (List<T>) getResourceService().getResources(Util.getResourceType(viewType),
        getResourceTypeModelClass(type));

    logger.trace("getResources({}) exit:: return {} item(s)", viewType, resources.size());
    return resources;
  }

  private ResourcesService getResourceService() {
    return ResourceServiceFactory.getInstance().getResourcesService();
  }

  private  Class<? extends ResourceModel> getResourceTypeModelClass(ResourceType type) {

    switch (type) {
        case user:
        case host:
          return RoleModel.class;
        case policy:
          return PolicyModel.class;
        case variable:
          return SecretModel.class;
        default:
      }

      return ResourceModel.class;
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
        resourceView = new ResourceViewImpl<>(type);
        break;
    }

    if (type != ViewType.Dashboard) { // no operation on dashboard
      resourceView.setSelectionListener(r -> toggleSelectionBasedAction(r != null));
      resourceView.setTableRowDoubleClickedEventListener(this::fireEditActionPerformed);
    }

    return (type == ViewType.Dashboard ? view :  resourceView);
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
    View view  = views.get(getCurrentViewType());
    if (view instanceof ResourceView) {
      fireActionPerformed((ResourceView)view);
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
    ((ResourceView)views.get(getCurrentViewType())).toggleSelectionBasedActions(enabled);
  }

  private ViewModel getViewModel(ViewType viewType) throws Exception {
    logger.trace("getViewModel enter:: {}", viewType);
    ViewModel model;

    switch(viewType) {
      case Hosts:
      case Users:
        model = new RoleTableModel(getResources(viewType));
        break;
      case Policies:
        model = new PolicyTableModel(getResources(viewType));
        break;
      case Secrets:
        model = getSecretsViewModel();
        break;
      case Dashboard:
        model = new DashboardViewModel(getAuditEvents(), getResourceCountMap());
        break;
      default:
        model = new DefaultResourceTableModel<>(getResources(viewType));
    }

    logger.trace("getViewModel exit:: return: {}", model);
    return model;
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

    for (ResourceType type: ResourceType.values()) {
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
   * @return ResourceTableModel
   * @throws ResourceAccessException In case of an error retrieving the secret
   */
  private SecretTableModel getSecretsViewModel() throws ResourceAccessException {
    logger.trace("getSecretsViewModel::enter::");

    List<SecretModel> secretModels = getResources(ViewType.Secrets);
    SecretTableModel model = new SecretTableModel(secretModels);
    Exception[] errors = new Exception[1];

    // Populate the model with latest secret version
    secretModels.forEach(i -> {
      try {
        if (i.secrets.length > 0) {
          i.secret = getResourceService().getSecret(i).toCharArray();
        }
      } catch (ResourceAccessException e) {
        if (e.getCause() instanceof FileNotFoundException) {
          logger.warn("Secret with id: {}}, has no value\nError: {}}", i.id, e.getCause());
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

  private void showErrorDialog(String msg) {
    JOptionPane.showMessageDialog(getMainForm(), msg, "Error",
        JOptionPane.ERROR_MESSAGE);
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
        getMainForm().getComponentView().setModel(getViewModel(view));
      } catch (Exception exception) {
        exception.printStackTrace();
        JOptionPane.showMessageDialog(getMainForm(), exception.getMessage(), "Error",
            JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  @Override
  public ViewType getCurrentViewType() {
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
