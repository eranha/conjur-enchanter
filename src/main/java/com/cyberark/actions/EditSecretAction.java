package com.cyberark.actions;

import com.cyberark.Util;
import com.cyberark.models.SecretModel;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@SelectionBasedAction
public class EditSecretAction extends EditItemAction<SecretModel> {
  private final static Map<Integer, String> errorCodes = getErrorCodeMapping();

  public EditSecretAction(Supplier<SecretModel> selectedResource) {
    this(selectedResource, "Edit");
  }
  public EditSecretAction(Supplier<SecretModel> selectedResource, String text) {
    super(selectedResource, text);
  }

  @Override
  public void actionPerformed(SecretModel secretModel) {
    String input = JOptionPane.showInputDialog(
        getMainForm(),
        "Update secret value?",
        new String(secretModel.getSecret())
    );

    if(Util.stringIsNotNullOrEmpty(input)) {
      try {
        getResourcesService().setSecret(secretModel, input);
        fireEvent(secretModel);
      } catch (Exception ex) {
        showErrorDialog(ex, errorCodes);
      }
    }
  }

  private static HashMap<Integer, String> getErrorCodeMapping() {
    HashMap<Integer, String> errors = new HashMap<>();
    errors.put(422, "error");
    errors.put(404, "variable");
    return errors;
  }
}
