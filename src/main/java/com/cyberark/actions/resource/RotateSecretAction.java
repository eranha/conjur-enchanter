package com.cyberark.actions.resource;

import com.cyberark.actions.ActionType;
import com.cyberark.models.SecretModel;
import com.cyberark.views.Icons;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static com.cyberark.Consts.DARK_BG;

@SelectionBasedAction
public class RotateSecretAction extends ActionBase<SecretModel> {
  private final static Map<Integer, String> errorCodes = getErrorCodeMapping();

  public RotateSecretAction(Supplier<SecretModel> selectedResource, String text) {
    super(text, ActionType.RotateSecret, selectedResource);
    putValue(SMALL_ICON, Icons.getInstance().getIcon(Icons.ICON_ROTATE_SECRET,
        16,
        DARK_BG));
    putValue(SHORT_DESCRIPTION, getString("rotate.secret.action.description"));
    setEnabled(false);
  }

  @Override
  public void actionPerformed(SecretModel secretModel) {

    JLabel message = new JLabel(
        getString("rotate.secret.action.label.text")
    );

    if(JOptionPane.showConfirmDialog(
        getMainForm(),
        message,
        getString("rotate.secret.action.approve.dialog.title"),
        JOptionPane.YES_NO_CANCEL_OPTION,
        JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION) {
      try {
        getResourcesService().rotateSecret(secretModel);
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
