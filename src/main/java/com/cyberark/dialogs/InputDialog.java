package com.cyberark.dialogs;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public class InputDialog extends JDialog {
  private final Component content;
  private final int optionType;
  int result = CANCEL_OPTION;
  public static final int OK_OPTION = 0;
  public static final int CANCEL_OPTION = 1;

  public InputDialog(Window owner, String title, boolean modal, Component content) {
    this(owner, title, modal, content, true);
  }

  public InputDialog(Window owner, String title, boolean modal, Component content, boolean isOkButtonEnabled) {
    this(owner, title, modal, content, isOkButtonEnabled, JOptionPane.OK_CANCEL_OPTION);
  }

  public InputDialog(Window owner, String title, boolean modal, Component content,
                     boolean isOkButtonEnabled, int optionType) {
    super(owner, title, modal ? ModalityType.APPLICATION_MODAL : ModalityType.MODELESS);
    this.optionType = optionType;
    this.content = content;
    initializeComponents();
    enableOkButton(isOkButtonEnabled);
  }

  public static int showDialog(Window owner, String title, boolean modal, Component content) {
    return showDialog(owner, title, modal, content, JOptionPane.OK_CANCEL_OPTION);
  }

  public static int showDialog(Window owner, String title, boolean modal, Component content, int optionType) {
    InputDialog dlg = new InputDialog(owner, title, modal, content, true, optionType);
    return dlg.showDialog();
  }

  public int showDialog() {
    setLocationRelativeTo(getOwner());
    pack();
    setLocationRelativeTo(getOwner());
    setVisible(true);
    return result;
  }

  public void enableOkButton(boolean enabled) {
    getRootPane().getDefaultButton().setEnabled(enabled);
  }

  private void initializeComponents() {
    JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
    JButton cancelButton = new JButton("Cancel");
    JButton okButton = new JButton("OK");

    getRootPane().setDefaultButton(okButton);
    getContentPane().setLayout(new BorderLayout());

    JPanel contentPane = new JPanel(new BorderLayout());
    contentPane.setBorder(BorderFactory.createEmptyBorder(16,16,8,10));
    contentPane.add(content, BorderLayout.CENTER);
    getContentPane().add(contentPane, BorderLayout.CENTER);
    getContentPane().add(controlPanel, BorderLayout.SOUTH);
    controlPanel.setBorder(BorderFactory.createEmptyBorder(8,0,8,8));
    controlPanel.add(cancelButton);
    controlPanel.add(okButton);

    okButton.addActionListener(e -> {
      result = OK_OPTION;
      setVisible(false);
    });

    cancelButton.addActionListener(e -> setVisible(false));

    getRootPane().registerKeyboardAction(e -> setVisible(false),
        KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
        JComponent.WHEN_IN_FOCUSED_WINDOW);


    if (optionType == JOptionPane.OK_OPTION) {
      cancelButton.setVisible(false);
    }
  }
}
