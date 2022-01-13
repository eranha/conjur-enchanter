package com.cyberark.actions.resource;

import com.cyberark.actions.ActionType;
import com.cyberark.exceptions.ResourceAccessException;
import com.cyberark.models.RoleModel;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@SelectionBasedAction
public class RotateApiKeyAction extends ActionBase<RoleModel> {
  private final static Map<Integer, String> errorCodes = getErrorCodeMapping();

  public RotateApiKeyAction(Supplier<RoleModel> selectedResource, String text) {
    super(text, ActionType.RotateApiKey, selectedResource);
    putValue(SHORT_DESCRIPTION, getString("rotate.api.key.action.description"));
    putValue(MNEMONIC_KEY, KeyEvent.VK_N);
    setEnabled(false);
  }

  @Override
  public void actionPerformed(RoleModel resource) {
    try {
      if (JOptionPane.showConfirmDialog(
          getMainForm(),
          getString("rotate.api.key.action.confirmation.message")) == JOptionPane.YES_OPTION) {
        String apiKey = getResourcesService().rotateApiKey(
            getSelectedResource().getIdentifier().getType(),
            getSelectedResource());
        promptToCopyApiKeyToClipboard(apiKey, getSelectedResource().getIdentifier());
      }
    } catch (ResourceAccessException ex) {
      showErrorDialog(ex, errorCodes);
    }
  }

  private static HashMap<Integer, String> getErrorCodeMapping() {
    HashMap<Integer, String> errors = new HashMap<>();
    errors.put(401, "rotate.api.key");
    return errors;
  }
}
