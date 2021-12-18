package com.cyberark.actions;

import com.cyberark.models.SecretModel;

import javax.swing.*;
import java.util.HashMap;
import java.util.function.Supplier;

@SelectionBasedAction
public class RotateSecretAction extends ActionBase<SecretModel> {
  public RotateSecretAction(Supplier<SecretModel> selectedResource) {
    this(selectedResource,"Rotate");
  }
  public RotateSecretAction(Supplier<SecretModel> selectedResource, String text) {
    super(text, ActionType.RotateSecret, selectedResource);
  }

  @Override
  public void actionPerformed(SecretModel secretModel) {

    JLabel message = new JLabel(
        "<html>" +
            "When a variable is configured for secret rotation, this API<br>" +
            "<span style='background-color: yellow'>" +
            "immediately expires the secret</span> value and rotates<br>" +
            "the existing secret value.<br>" +
            "Are you sure you want to rotate this secret?" +
            "</html>"
    );

    if(JOptionPane.showConfirmDialog(
        getMainForm(), message,
        "Approve Secret Rotations",
        JOptionPane.YES_NO_CANCEL_OPTION,
        JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION) {
      try {
        getResourcesService().rotateSecret(secretModel);
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
