package com.cyberark.dialogs;

import com.cyberark.Consts;
import com.cyberark.components.DefaultDocumentListener;
import com.cyberark.event.LoginEventResult;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Objects;

public class LoginDialog extends JDialog implements ActionListener {

  private static final String LOGIN = "login";
  JButton loginBtn = new JButton("Login");
  ActionListener actionListener;
  HashMap<String, Document> formInputs = new HashMap<>();
  JLabel statusLabel = new JLabel();
  Color defaultLabelColor;

  public LoginDialog(JFrame owner, ActionListener actionListener) {
    super(owner, true);
    this.actionListener = actionListener;
    getContentPane().setLayout(new GridBagLayout());
    addComponentsToContainer();
    setTitle(String.format("%s - Login", Consts.APP_NAME));
    setBounds(10, 10, 360, 480);
    setResizable(false);
    loginBtn.addActionListener(this);
    defaultLabelColor = statusLabel.getForeground();
    final Toolkit toolkit = Toolkit.getDefaultToolkit();
    final Dimension screenSize = toolkit.getScreenSize();
    final int x = (screenSize.width - getWidth()) / 2;
    final int y = (screenSize.height - getHeight()) / 2;
    setLocation(x, y);
    getRootPane().setDefaultButton(loginBtn);
    setVisible(true);
  }

  public void setErrorStatus(String msg) {
    statusLabel.setForeground(Color.RED);
    statusLabel.setText(msg);
  }

  public void addComponentsToContainer() {
    JPanel formPanel = new JPanel();
    JPasswordField passwordField = new JPasswordField();
    JTextField userTextField = new JTextField();
    JTextField urlTextField = new JTextField();
    JTextField accountTextField = new JTextField();
    GridBagConstraints constraints = new GridBagConstraints();
    int gridy = 0;
    Insets zeroInsets = new Insets(0, 0, 0, 0);

    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.gridx = 0;
    constraints.gridy = 0;
    getContentPane().add(new JLabel(new ImageIcon(
        Objects.requireNonNull(getClass().getClassLoader().getResource("conjur.jpg")))), constraints);

    constraints.fill = GridBagConstraints.BOTH;
    constraints.gridx = 0;
    constraints.gridy = 1;
    constraints.weighty = 1;
    constraints.weightx = 1;
    constraints.insets = new Insets(8, 4, 0, 4);
    formPanel.setLayout(new GridBagLayout());
    getContentPane().add(formPanel, constraints);


    constraints.gridwidth = 1;
    constraints.gridheight = 1;

    // URL
    constraints.gridy = gridy++;
    Insets labelInsets = new Insets(0, 6, 0, 4);
    constraints.insets = labelInsets;
    formPanel.add(createLabel(urlTextField, "URL"), constraints);

    constraints.gridy = gridy++;
    constraints.insets = zeroInsets;
    addChangeListener("url", urlTextField);
    formPanel.add(urlTextField, constraints);

    // ACCOUNT
    constraints.gridy = gridy++;
    constraints.insets = labelInsets;
    formPanel.add(createLabel(accountTextField, "Account"), constraints);

    constraints.gridy = gridy++;
    constraints.insets = zeroInsets;
    addChangeListener("account", accountTextField);
    formPanel.add(accountTextField, constraints);

    // USERNAME
    constraints.gridy = gridy++;
    constraints.insets = labelInsets;
    formPanel.add(createLabel(userTextField, "Username"), constraints);

    constraints.gridy = gridy++;
    constraints.insets = zeroInsets;
    addChangeListener("user", userTextField);
    formPanel.add(userTextField, constraints);

    //PASSWORD
    constraints.gridy = gridy++;
    constraints.insets = labelInsets;
    formPanel.add(createLabel(passwordField,"Password"), constraints);

    constraints.gridy = gridy++;
    constraints.insets = zeroInsets;
    passwordField.setName("password");
    addChangeListener("password", passwordField);
    formPanel.add(passwordField, constraints);

    constraints.fill = GridBagConstraints.BOTH;
    constraints.gridx = 0;
    constraints.gridy = gridy++;
    constraints.insets = new Insets(16, 4, 8, 4);
    loginBtn.setActionCommand(LOGIN);
    formPanel.add(loginBtn, constraints);

    constraints.gridy = gridy;
    formPanel.add(statusLabel, constraints);

  }

  private JLabel createLabel(Component forComponent, String text) {
    JLabel l = new JLabel(text);
    l.setLabelFor(forComponent);
    l.setFont(new Font("Verdana", Font.PLAIN, 12));
    return  l;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (LOGIN.equals(e.getActionCommand())) {
      actionListener.actionPerformed(
          new LoginEventResult(
              this,
              getUrl(),
              getAccount(),
              getUser(),
              getPassword()
          ));
    }
  }

  private char[] getPassword() {
    return getFormInput("password").toCharArray();
  }

  private String getUser() {
    return getFormInput("user");
  }

  private String getAccount() {
    return getFormInput("account");
  }

  private String getUrl() {
    return getFormInput("url");
  }

  private String getFormInput(String name) {
    return getText(formInputs.get(name));
  }

  private static String getText(Document doc) {
    try {
      return doc.getText(0, doc.getLength());
    } catch (BadLocationException e) {
      e.printStackTrace();
    }
    return null;
  }

  private void addChangeListener(String name, JTextComponent textField) {
    formInputs.put(name, textField.getDocument());
    textField.getDocument().addDocumentListener(new DefaultDocumentListener(e-> {
      Component comp = (Component) e.getDocument().getProperty("owner");
      loginBtn.setEnabled(formInputs.values().stream().allMatch(doc-> {
        try {
          return doc.getText(0, doc.getLength()).length() > 0;
        } catch (BadLocationException badLocationException) {
          badLocationException.printStackTrace();
        }
        return false;
      }));
    }));
  }
}