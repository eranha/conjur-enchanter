package com.cyberark.actions;

import com.cyberark.Util;
import com.cyberark.models.SecretModel;

import javax.swing.*;
import java.util.function.Supplier;

@SelectionBasedAction
public class EditSecretAction extends EditItemAction<SecretModel> {
  public EditSecretAction(Supplier<SecretModel> selectedRsource) {
    this(selectedRsource, "Edit");
  }
  public EditSecretAction(Supplier<SecretModel> selectedRsource, String text) {
    super(selectedRsource, text);
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
        showErrorDialog(ex.getMessage());
      }
    }
  }
}
