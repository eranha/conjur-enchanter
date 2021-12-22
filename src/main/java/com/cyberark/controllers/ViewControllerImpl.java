package com.cyberark.controllers;

import com.cyberark.Application;
import com.cyberark.Util;
import com.cyberark.actions.ViewNavigationAction;
import com.cyberark.components.ApiCallLogView;
import com.cyberark.components.MainForm;
import com.cyberark.event.*;
import com.cyberark.exceptions.ResourceAccessException;
import com.cyberark.models.DashboardViewModel;
import com.cyberark.models.ResourceIdentifier;
import com.cyberark.models.ResourceModel;
import com.cyberark.models.ResourceType;
import com.cyberark.models.audit.AuditEvent;
import com.cyberark.resource.ResourceServiceFactory;
import com.cyberark.resource.ResourcesService;
import com.cyberark.views.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.URI;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.cyberark.Util.getViewType;

class ViewControllerImpl implements ViewController {
  private static final Logger logger = LogManager.getLogger(ViewControllerImpl.class);
  private ViewType currentViewType;
  private final ResourceViewController resourceViewController = new ResourceViewControllerImpl();
  DashboardView dashboardView;

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
      if (viewType == ViewType.Dashboard) {
        view = getDashboardView();
      } else {
        view = getResourceView(viewType);
      }
    } catch (Exception e) {
      logger.error(e);
      e.printStackTrace();
      ErrorView.showErrorMessage(e);
    }

    logger.trace("getView exit");
    return view;
  }

  private ResourcesService getResourceService() {
    return ResourceServiceFactory.getInstance().getResourcesService();
  }

  private View getDashboardView() throws ResourceAccessException {
    if (dashboardView == null) {
      dashboardView = new DashboardView(this::actionPerformed);
    }
    dashboardView.setModel(
        new DashboardViewModel(getAuditEvents(), getResourceCountMap())
    );
    return dashboardView;
  }

  private View getResourceView(ViewType type) throws ResourceAccessException {
    ResourceView view = resourceViewController.getResourceView(type);
    resourceViewController.setResourceViewModel(view.getType());
    return view;
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

    return currentViewType == ViewType.Dashboard
        ? dashboardView
        : resourceViewController.getResourceView(currentViewType);
  }

  private void setViewModel(ViewType viewType) throws Exception {
    logger.trace("setViewModel enter:: {}", viewType);

    if (viewType == ViewType.Dashboard) {
      dashboardView.setModel(
          new DashboardViewModel(getAuditEvents(), getResourceCountMap())
      );
    } else {
      resourceViewController.setResourceViewModel(
        viewType
      );
    }

    logger.trace("setViewModel exit::");
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

    Stream<ResourceType> resourceTypeStream = Arrays
        .stream(ResourceType.values())
        .filter(t -> t != ResourceType.host_factory);

    for (ResourceType type : resourceTypeStream.collect(Collectors.toList())) {
      map.put(type, getResourceCount(type));
    }


    logger.trace("getResourceCountMap::exit:: return {}", map);
    return map;
  }

  private Integer getResourceCount(ResourceType type) throws ResourceAccessException {
    return getResourceService().getResourcesCount(type);
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
    resourceViewController.setViewType(currentViewType);
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
    dashboardView.clearData();
    resourceViewController.clearData();
  }

  private MainForm getMainForm() {
    return Application.getInstance().getMainForm();
  }
}
