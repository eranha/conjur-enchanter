package com.cyberark.actions.resource;

import com.cyberark.Application;
import com.cyberark.Util;
import com.cyberark.actions.ActionType;
import com.cyberark.actions.ActionUtil;
import com.cyberark.controllers.ControllerFactory;
import com.cyberark.controllers.ViewController;
import com.cyberark.event.EventPublisher;
import com.cyberark.event.ResourceEvent;
import com.cyberark.exceptions.ApiCallException;
import com.cyberark.exceptions.ResourceAccessException;
import com.cyberark.models.Membership;
import com.cyberark.models.ResourceIdentifier;
import com.cyberark.models.ResourceModel;
import com.cyberark.resource.ResourceServiceFactory;
import com.cyberark.resource.ResourcesService;
import com.cyberark.util.Resources;
import com.cyberark.views.ErrorView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.function.Supplier;

import static com.cyberark.Consts.ACTION_TYPE_KEY;
import static com.cyberark.Consts.RESOURCES_INFO_PROPERTIES;

/**
 * Base action of all actions that gets performed an a selected resource model.
 * @param <T> The resource model
 */
public abstract class ActionBase<T extends ResourceModel> extends AbstractAction implements ResourceAction {
  private final Supplier<T> selectedResource;

  protected ActionBase(String text,
                       ActionType type,
                       Supplier<T> selectedResource) {
    super(text);
    this.selectedResource = Objects.requireNonNull(selectedResource);
    putValue(ACTION_TYPE_KEY, type);
  }

  protected static String getString(String key) {
    return Resources.getString(key);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    actionPerformed(Objects.requireNonNull(getSelectedResource()));
  }

  protected abstract void actionPerformed(T selectedResource);

  protected ResourcesService getResourcesService() {
    return ResourceServiceFactory
            .getInstance()
            .getResourcesService();
  }

  protected void fireEvent() {
    fireEvent(null);
  }

  protected void fireEvent(ResourceModel model) {
    EventPublisher.getInstance().fireEvent(new ResourceEvent<>(model));
  }

  protected Frame getMainForm() {
    return Application.getInstance().getMainForm();
  }

  protected void showErrorDialog(Exception ex) {
    showErrorDialog(ex, new HashMap<>());
  }

  protected void showErrorDialog(Exception ex, Map<Integer, String> errors) {
    ex.printStackTrace();

    if (ex.getCause() instanceof ApiCallException) {
      ErrorView.showApiCallErrorMessage((ApiCallException) ex.getCause(), errors);
    } else {
      ErrorView.showErrorMessage(ex.toString());
    }
  }

  protected ViewController getViewController() {
    return ControllerFactory.getInstance().getViewController();
  }

  protected T getSelectedResource() {
    return selectedResource.get();
  }

  protected void promptToCopyApiKeyToClipboard(String response, ResourceIdentifier model) {
    ActionUtil.promptToCopyApiKeyToClipboard(getMainForm(), response, model);
    getViewController().setStatusLabel(getString("copy.api.key.to.clipboard.status.label"));
  }

  @Override
  public ActionType getActionType() {
    return (getValue(ACTION_TYPE_KEY) == null || !(getValue(ACTION_TYPE_KEY) instanceof ActionType))
        ? null
        : (ActionType) getValue(ACTION_TYPE_KEY);
  }

  @Override
  public boolean isSelectionBased() {
    return Arrays.stream(getClass().getDeclaredAnnotations()).anyMatch(a -> a instanceof SelectionBasedAction);
  }

  protected List<Membership> getMembers(ResourceIdentifier resourceIdentifier) throws ResourceAccessException {
    try {
      return getResourcesService().getMembers(resourceIdentifier);
    } catch (ResourceAccessException e) {
      handleRoleError(e);
    }
    return null;
  }

  private void handleRoleError(ResourceAccessException e) throws ResourceAccessException {
    HashMap<Integer, String> errors = new HashMap<>();
    errors.put(404, "role");
    showErrorDialog((ApiCallException) e.getCause(), errors);
    throw e;
  }

  protected List<Membership> getMembership(ResourceIdentifier resourceIdentifier) throws ResourceAccessException {
    try {
      return getResourcesService().getMembership(resourceIdentifier);
    } catch (ResourceAccessException e) {
      handleRoleError(e);
    }
    return null;
  }

  protected Properties getResourcesInfo() {
    Properties info = new Properties();

    try {
      info.load(Util.getProperties(RESOURCES_INFO_PROPERTIES));
    } catch (IOException e) {
      e.printStackTrace();
    }
    return info;
  }
}
