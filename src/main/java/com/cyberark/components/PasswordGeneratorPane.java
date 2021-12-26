package com.cyberark.components;

import com.cyberark.views.Icons;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

import static com.cyberark.Consts.DARK_BG;
import static com.cyberark.Util.generatePassword;

public class PasswordGeneratorPane extends JPanel {
  private final JTextField textField = new JTextField(24);

  public PasswordGeneratorPane(String initialValue) {
    initializeComponents(initialValue);
  }

  private void initializeComponents(String initialValue) {
    setLayout(new BorderLayout());
    JPanel panel = new JPanel(new BorderLayout());
    JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
    JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
    JButton generatePassword = new JButton(Icons.getInstance().getIcon(Icons.ICON_ROTATE_SECRET, 16, DARK_BG));

    if (Objects.nonNull(initialValue)) {
      textField.setText(initialValue.trim());
    }

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
    this.add(panel, BorderLayout.CENTER);
  }

  public static PasswordGeneratorDialogResult showDialog(
      Component parentComponent,
      String title,
      int optionType,
      String initialValue) {
    return showDialog(parentComponent, title, optionType, initialValue, null);
  }

  public static PasswordGeneratorDialogResult showDialog(
      Component parentComponent,
      String title,
      int optionType,
      String initialValue,
      String tooltip) {

    PasswordGeneratorPane pane = new PasswordGeneratorPane(initialValue);
    pane.textField.setToolTipText(tooltip);

    return new PasswordGeneratorDialogResult(JOptionPane.showConfirmDialog(
        parentComponent,
        pane,
        title,
        optionType
    ), pane.textField.getText().trim());
  }

  public static class PasswordGeneratorDialogResult {
    private int result;
    private char[] password;

    public PasswordGeneratorDialogResult(int result, String password) {
      this.result = result;
      this.password = password.toCharArray();
    }

    public int getResult() {
      return result;
    }

    public char[] getPassword() {
      return password;
    }
  }
}
