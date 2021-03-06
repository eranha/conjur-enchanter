package com.cyberark.actions.resource;

import com.cyberark.Util;
import com.cyberark.actions.ActionType;
import com.cyberark.components.PasswordGeneratorPane;
import com.cyberark.exceptions.ResourceAccessException;
import com.cyberark.models.ResourceType;
import com.cyberark.models.RoleModel;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@SelectionBasedAction
public class UpdatePasswordAction extends ActionBase<RoleModel> {
  private final static Map<Integer, String> errorCodes = getErrorCodeMapping();

  public UpdatePasswordAction(Supplier<RoleModel> selectedResource, String text) {
    super(text, ActionType.UpdatePassword, selectedResource);
    putValue(SHORT_DESCRIPTION,
        getString("update.password.menu.action.description"));
    putValue(MNEMONIC_KEY, KeyEvent.VK_P);
    setEnabled(false);
  }

  @Override
  public void actionPerformed(RoleModel resource) {
    try {
      if (JOptionPane.showConfirmDialog(
          getMainForm(),
          new JLabel(getString("update.password.menu.action.dialog.message"))
      ) == JOptionPane.YES_OPTION) {

        String tooltip = getString("update.password.menu.action.password.generator.tooltip");

        PasswordGeneratorPane.PasswordGeneratorDialogResult result = PasswordGeneratorPane.showDialog(
            getMainForm(),
            getString("update.password.menu.action.dialog.title"),
            JOptionPane.OK_CANCEL_OPTION,
            Util.generatePassword(),
            tooltip
        );

        String password = result.getResult() == JOptionPane.OK_OPTION
            ? new String(result.getPassword())
            : null;

        if (Util.nonNullOrEmptyString(password)) {
          String apiKey = getResourcesService().rotateApiKey(
              getSelectedResource().getIdentifier().getType(),
              getSelectedResource());

          getResourcesService().updateUserPassword(
              resource,
              password.toCharArray(),
              apiKey.toCharArray()
          );

          // Update password also replaces the user???s API key with a new securely generated random value.
          // Fetch the new API key by using Login and prompt it to the user
          apiKey = getResourcesService().getApiKey(resource, password.toCharArray());
          promptToCopyApiKeyToClipboard(apiKey, getSelectedResource().getIdentifier());
        }
      }
    } catch (ResourceAccessException ex) {
      showErrorDialog(ex, errorCodes);
    }
  }

  @Override
  public void setEnabled(boolean enabled) {
    RoleModel selectedResource = getSelectedResource();
    boolean isUser = selectedResource != null &&
                     selectedResource.getIdentifier().getType() == ResourceType.user;
    super.setEnabled(enabled && isUser);
  }

  private static HashMap<Integer, String> getErrorCodeMapping() {
    HashMap<Integer, String> errors = new HashMap<>();
    errors.put(422, "update.password");
    errors.put(404, "update.password");
    return errors;
  }
}
