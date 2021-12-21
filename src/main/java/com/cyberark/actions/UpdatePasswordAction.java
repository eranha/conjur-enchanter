package com.cyberark.actions;

import com.cyberark.Util;
import com.cyberark.exceptions.ResourceAccessException;
import com.cyberark.models.ResourceType;
import com.cyberark.models.RoleModel;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@SelectionBasedAction
public class UpdatePasswordAction extends ActionBase<RoleModel> {
  private final static Map<Integer, String> errorCodes = getErrorCodeMapping();

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

        String password = JOptionPane.showInputDialog(getMainForm(),
            new JLabel(
                "<html>" +
                     "Type in a new password or submit the <b>random generated</b> password.<br>" +
                     "Choose a password that includes: 12-128 characters, 2 uppercase letters, <br>" +
                     "2 lowercase letters,1 digit and 1 special character.<br></html>"
            ),
            generatePassword());

        if (Util.stringIsNotNullOrEmpty(password)) {
          String apiKey = getResourcesService().rotateApiKey(
              getSelectedResource().getIdentifier().getType(),
              getSelectedResource());

          getResourcesService().updateUserPassword(
              resource,
              password.toCharArray(),
              apiKey.toCharArray()
          );

          // Update password also replaces the user’s API key with a new securely generated random value.
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
    if (enabled && getSelectedResource().getIdentifier().getType() != ResourceType.user) {
      super.setEnabled(false);
    } else {
      super.setEnabled(enabled);
    }
  }

  private static HashMap<Integer, String> getErrorCodeMapping() {
    HashMap<Integer, String> errors = new HashMap<>();
    errors.put(422, "update.password");
    errors.put(404, "update.password");
    return errors;
  }

  private static String generatePassword() {
    int length = 32;
    String uppers = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    String lowers = "abcdefghijklmnopqrstuvwxyz";
    String digits = "0123456789";
    String special = "!@@#$%^&*(){}[]?/`~\\|':;\"";

    StringBuilder password = null;
    StringBuilder charset = new StringBuilder();

    charset
        .append(uppers)
        .append(lowers)
        .append(digits)
        .append(special);
    boolean atLeastOneDigit = false;
    boolean atLeastOneSpecial = false;
    int uppersCount = 0;
    int lowersCount = 0;
    boolean allConditionsAreMet = false;

    while (!allConditionsAreMet) {
      password = new StringBuilder();

      for (int i = 0, n = charset.length(); i < length; ++i) {
        char ch = charset.charAt((int)Math.floor(Math.random() * n));

        if (Character.isLetter(ch)) {
          if (Character.isLowerCase(ch)) {
            lowersCount++;
          } else {
            uppersCount++;
          }
        } else if (Character.isDigit(ch)) {
          atLeastOneDigit = true;
        } else if (
            Arrays
              .stream(special.split(""))
              .anyMatch(s -> s.charAt(0) == ch)
        ) {
          atLeastOneSpecial = true;
        }

        password.append(ch);
      }

      allConditionsAreMet = atLeastOneDigit && atLeastOneSpecial && lowersCount >= 2 && uppersCount >= 2;

    }

    return password.toString();
  }
}
