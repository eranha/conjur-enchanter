package com.cyberark.actions;

import com.cyberark.exceptions.ResourceAccessException;
import com.cyberark.models.ResourceType;
import com.cyberark.models.RoleModel;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.function.Supplier;

@SelectionBasedAction
public class UpdatePasswordAction extends ActionBase<RoleModel> {
  public UpdatePasswordAction(Supplier<RoleModel> selectedResource, String text) {
    super(text, ActionType.UpdatePassword, selectedResource);
    putValue(SHORT_DESCRIPTION,
        "Changes a user’s password.");
    putValue(MNEMONIC_KEY, KeyEvent.VK_P);
  }

  @Override
  public void actionPerformed(RoleModel resource) {
    try {
      if (JOptionPane.showConfirmDialog(
          getMainForm(),
          new JLabel("<html>Update password requires " +
              "<span style=\"background-color:yellow\">rotations of the user's API key</span>.<br>" +
              "Are you sure you want to continue?</html>")
      ) == JOptionPane.YES_OPTION) {
        String apiKey = getResourcesService().rotateApiKey(
            getSelectedResource().getIdentifier().getType(),
            getSelectedResource());
        String password = JOptionPane.showInputDialog(getMainForm(), "Type in new password.");

        if (password != null) {
          getResourcesService().updateUserPassword(resource, password.toCharArray(), apiKey.toCharArray());

          // Update password also replaces the user’s API key with a new securely generated random value.
          // Fetch the new API key by using Login and prompt it to the user
          apiKey = getResourcesService().getApiKey(resource, password.toCharArray());
          promptToCopyApiKeyToClipboard(apiKey, getSelectedResource().getIdentifier());
        }
      }
    } catch (ResourceAccessException ex) {
      showErrorDialog(ex.getMessage());
    }
  }

  @Override
  public void setEnabled(boolean enabled) {
    if (enabled && getSelectedResource().getIdentifier().getType() != ResourceType.user) {
      super.setEnabled(false);
    } else {
      super.setEnabled(enabled);
    }
  }
}
