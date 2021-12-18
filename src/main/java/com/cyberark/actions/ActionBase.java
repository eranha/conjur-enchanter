package com.cyberark.actions;

import com.cyberark.Application;
import com.cyberark.Util;
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
import com.cyberark.views.ErrorView;
import com.fasterxml.jackson.core.JsonProcessingException;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import static com.cyberark.Consts.ACTION_TYPE_KEY;

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
    Objects.requireNonNull(selectedResource);
    this.selectedResource = selectedResource;
    putValue(ACTION_TYPE_KEY, type);
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
    String apiKey;

    try {
      apiKey = Util.extractApiKey(
          response,
          model.getFullyQualifiedId()
      );
    } catch (JsonProcessingException e) {
      /* response is the api key not a json */
      apiKey = response;
    }

    if (apiKey == null) {
      JOptionPane
          .showMessageDialog(getMainForm(),
              String.format("Rotate the API key of role '%s' to get the its value.", model.getId()),
              "API Key",
              JOptionPane.INFORMATION_MESSAGE);
      return;
    }

    JLabel label = new JLabel("Click Copy to copy the API key to clipboard.");
    JLabel label2 = new JLabel("(you will only see this once)");
    JTextField jt = new JTextField(apiKey);
    jt.addAncestorListener(new AncestorListener()
    {
      public void ancestorAdded ( AncestorEvent event )
      {
        jt.requestFocus();
        jt.selectAll();
      }
      public void ancestorRemoved ( AncestorEvent event ) {}
      public void ancestorMoved ( AncestorEvent event ) {}
    });

    Object[] choices = {"Copy", "Close"};
    Object defaultChoice = choices[0];

    int answer = JOptionPane
        .showOptionDialog(getMainForm(), new Component[]{label, label2,jt}, "Copy API Key to Clipboard?",
            JOptionPane.YES_NO_CANCEL_OPTION,//
            JOptionPane.QUESTION_MESSAGE, null, choices, defaultChoice);

    if (answer == JOptionPane.YES_OPTION) {
      Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      clipboard.setContents(new StringSelection(apiKey), null);
    }

    getViewController().setStatusLabel("API key has been copied to clipboard.");
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
}
