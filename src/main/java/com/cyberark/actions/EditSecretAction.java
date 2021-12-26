package com.cyberark.actions;

import com.cyberark.Util;
import com.cyberark.models.SecretModel;
import com.cyberark.views.Icons;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static com.cyberark.Consts.DARK_BG;
import static com.cyberark.Util.generatePassword;

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
    JPanel panel = new JPanel(new BorderLayout());
    JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
    JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
//    panel.setBorder(
//        BorderFactory.createTitledBorder(new EmptyBorder(0,0,0,0),
//        "Enter new value:")
//    );
    JTextField textField = new JTextField(new String(secretModel.getSecret()), 24);
    JButton generatePassword = new JButton(Icons.getInstance().getIcon(Icons.ICON_ROTATE_SECRET, 16, DARK_BG));
    generatePassword.setToolTipText("Generate random secure string");
    topPanel.add(Box.createHorizontalStrut(4));
    topPanel.add(new JLabel("<html>Enter new value or click the button on the right" +
        "<br>to generate a random secure string:<html>"));
    panel.add(topPanel, BorderLayout.NORTH);
    panel.add(bottomPanel, BorderLayout.CENTER);
    bottomPanel.add(textField);
    bottomPanel.add(Box.createHorizontalStrut(8));
    bottomPanel.add(generatePassword);

    generatePassword.addActionListener(e -> {
      textField.setText(generatePassword());
      textField.requestFocus();
      textField.selectAll();
    });

    int result = JOptionPane.showConfirmDialog(
        getMainForm(),
        panel,
        "Set Secret Value",
        JOptionPane.OK_CANCEL_OPTION
    );

    if(result == JOptionPane.OK_OPTION && Util.stringIsNotNullOrEmpty(textField.getText().trim())) {
      try {
        getResourcesService().setSecret(secretModel, textField.getText().trim());
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
