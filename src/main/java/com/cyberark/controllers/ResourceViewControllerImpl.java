package com.cyberark.controllers;

import com.cyberark.Util;
import com.cyberark.actions.ActionType;
import com.cyberark.exceptions.ResourceAccessException;
import com.cyberark.models.HostFactory;
import com.cyberark.models.SecretModel;
import com.cyberark.models.table.*;
import com.cyberark.resource.ResourceServiceFactory;
import com.cyberark.resource.ResourcesService;
import com.cyberark.views.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResourceViewControllerImpl implements ResourceViewController {
  private static final Logger logger = LogManager.getLogger(ResourceViewControllerImpl.class);
  private final Map<ViewType, ResourceView> views = new HashMap<>();
  private ViewType currentViewType;


  @Override
  public ResourceView getResourceView(ViewType type) {
    if (type == ViewType.Dashboard) {
      return null;
    }

    views.computeIfAbsent(type, this::createResourceView);
    return views.get(type);
  }

  @Override
  public void setResourceViewModel(ViewType type) throws ResourceAccessException {
    if (type != ViewType.Dashboard) {
      views.computeIfAbsent(type, this::createResourceView);
    }

    switch (type) {
      case Policies:
        getPoliciesView().setResourceTableModel(
            new PolicyTableModel(getResourceService().getPolicies())
        );
        break;
      case Hosts:
      case Users:
        getRolesView(type).setResourceTableModel(
            new RoleTableModel(
                getResourceService().getRoles(Util.getResourceType(type))
            )
        );
        break;
      case Secrets:
        getSecretsView().setResourceTableModel(
            getSecretsViewModel()
        );
        break;
      case HostFactories:
        getHostFactoriesView().setResourceTableModel(
            getHostFactoriesViewModel()
        );
        break;      case Webserivices:
      case Layers:
      case Groups:
        getResourcesView(type).setResourceTableModel(
            new DefaultResourceTableModel<>(
                getResourceService().getResources(Util.getResourceType(type))
            )
        );
        break;
    }
  }

  private ResourceTableModel<HostFactory> getHostFactoriesViewModel() throws ResourceAccessException {
    logger.trace("getHostFactoriesViewModel::enter::");
    ResourceTableModel<HostFactory> model = new DefaultResourceTableModel<>(getResourceService().getHostFactories());
    logger.trace("getSecretsViewModel::exit:: return: {}", model);
    return model;
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


  private ResourcesService getResourceService() {
    return ResourceServiceFactory.getInstance().getResourcesService();
  }

  private CommonResourceView getResourcesView(ViewType viewType) {
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

  private HostFactoriesView getHostFactoriesView() {
    return (HostFactoriesView) views.get(ViewType.HostFactories);
  }

  public ResourceView createResourceView(ViewType type) {
    ResourceView resourceView;

    switch (type) {
      case Policies:
        resourceView = new PoliciesView();
        break;
      case Secrets:
        resourceView = new SecretsView();
        break;
      case HostFactories:
        resourceView = new HostFactoriesView();
        break;
      case Users:
      case Hosts:
        resourceView = new RolesView(type);
        break;
      default:
        resourceView = new CommonResourceView(type);
        break;
    }

    resourceView.setSelectionListener(r -> toggleSelectionBasedAction(r != null));
    resourceView.setTableRowDoubleClickedEventListener(this::fireEditActionPerformed);

    return resourceView;
  }

  private void toggleSelectionBasedAction(boolean enabled) {
    if (getCurrentView() != null) {
      getCurrentView().toggleSelectionBasedActions(enabled);
    }

  }

  @Override
  public void setViewType(ViewType viewType) {
    this.currentViewType = viewType != ViewType.Dashboard
        ? viewType
        : null;
  }

  @Override
  public void clearData() {
    views.values().forEach(View::clearData);
  }

  private ResourceView getCurrentView() {
    return currentViewType != null
            ? views.get(currentViewType)
            : null;
  }

  private void fireEditActionPerformed(ResourceView view) {
    Action action = view.getAction(ActionType.EditItem);

    if (action != null) {
      action.actionPerformed(
          new ActionEvent(this, ActionEvent.ACTION_FIRST,
              ActionType.EditItem.toString()));
    }
  }
}
