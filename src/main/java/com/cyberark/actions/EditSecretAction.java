package com.cyberark.actions;

import com.cyberark.Util;
import com.cyberark.models.SecretModel;

import javax.swing.*;
import java.util.HashMap;
import java.util.function.Supplier;

@SelectionBasedAction
public class EditSecretAction extends EditItemAction<SecretModel> {
  public EditSecretAction(Supplier<SecretModel> selectedResource) {
    this(selectedResource, "Edit");
  }
  public EditSecretAction(Supplier<SecretModel> selectedResource, String text) {
    super(selectedResource, text);
  }

  @Override
  public void actionPerformed(SecretModel secretModel) {
    String input = JOptionPane.showInputDialog(getMainForm(),
        "Update secret value?", secretModel.secret);
    if(Util.stringIsNotNullOrEmpty(input)) {
      try {
        getResourcesService().setSecret(secretModel, input);
        fireEvent(secretModel);
      } catch (Exception ex) {
        HashMap<Integer, String> errors = new HashMap<>();
        errors.put(422, "error");
        errors.put(404, "variable");
        showErrorDialog(ex, errors);
      }
    }
  }
}
