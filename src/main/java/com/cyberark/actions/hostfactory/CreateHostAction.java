package com.cyberark.actions.hostfactory;

import com.cyberark.Util;
import com.cyberark.actions.ActionType;
import com.cyberark.actions.resource.SelectionBasedAction;
import com.cyberark.components.HostFactoryHostForm;
import com.cyberark.dialogs.InputDialog;
import com.cyberark.models.HostFactory;
import com.cyberark.models.ResourceIdentifier;
import com.cyberark.models.ResourceType;
import com.cyberark.views.ViewFactory;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.Arrays;
import java.util.function.Supplier;

@SelectionBasedAction
public class CreateHostAction extends AbstractHostFactoryAction {

  public CreateHostAction(Supplier<HostFactory> selectedResource) {
    super(ActionType.CreateHost, selectedResource, "Create Host...");
  }

  @Override
  public void actionPerformed(HostFactory model) {
    if (model.getTokens() == null || model.getTokens().length == 0) {
      ViewFactory.getInstance().getMessageView().showMessageDialog(
          "<html>Creating a host, requires a Host Factory Token.<br>First create a token and than retry.</html>"
      );
      return;
    }

    if (Arrays.stream(model.getTokens())
        .noneMatch(
          t -> Instant.parse( t.expiration ).isAfter(Instant.now()))
        ) {
      ViewFactory.getInstance().getMessageView().showMessageDialog(
       "All tokens are expired. Creates one or more tokens and retry."
      );
      return;
    }

    HostFactoryHostForm form = new HostFactoryHostForm(model.getTokens());

    InputDialog dialog = new InputDialog(
      getMainForm(),
      "Host Factory - Create Host",
      true,
      form,
      false
    );

    form.setPropertyChangeListener(e -> dialog.enableOkButton(
        Util.stringIsNotNullOrEmpty(form.getHostName()) && Util.stringIsNotNullOrEmpty(form.getSelectedToken())
    ));

    if (dialog.showDialog() == InputDialog.OK_OPTION) {
      try {
        String response = getResourcesService().createHostFactoryHost(form.getHostName(), form.getSelectedToken());
        ResourceIdentifier id =ResourceIdentifier.fromString(
            model.getIdentifier().getAccount(),
            ResourceType.host,
            form.getHostName());
        JsonNode api_key = Util.getNode(response, "api_key");
        promptToCopyApiKeyToClipboard(api_key.asText(), id);
        fireEvent(model);
      } catch (Exception e) {
        showErrorDialog(e, getErrorCodeMapping());
      }
    }
  }
}
