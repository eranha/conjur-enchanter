package com.cyberark.actions;

import com.cyberark.exceptions.ResourceAccessException;
import com.cyberark.models.ResourceModel;
import com.cyberark.models.RoleModel;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.function.Supplier;

@SelectionBasedAction
public class RotateApiKeyAction extends ActionBase<RoleModel> {
  @SuppressWarnings("unchecked")
  public <T extends ResourceModel> RotateApiKeyAction(Supplier<T> selectedResource, String text) {
    super(text, ActionType.RotateApiKey, (Supplier<RoleModel>) selectedResource);
    putValue(SHORT_DESCRIPTION,
        "Replaces API key with a new, securely random API key");
    putValue(MNEMONIC_KEY, KeyEvent.VK_N);
  }

  @Override
  public void actionPerformed(RoleModel resource) {
    try {
      if (JOptionPane.showConfirmDialog(
          getMainForm(),
          "Are you sure you want to rotate the API key?") == JOptionPane.YES_OPTION) {
        String apiKey = getResourcesService().rotateApiKey(
            getSelectedResource().getIdentifier().getType(),
            getSelectedResource());
        promptToCopyApiKeyToClipboard(apiKey, getSelectedResource().getIdentifier());
      }
    } catch (ResourceAccessException ex) {
      showErrorDialog(ex.getMessage());
    }
  }
}
