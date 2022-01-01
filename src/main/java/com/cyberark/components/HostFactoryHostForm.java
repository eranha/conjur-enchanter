package com.cyberark.components;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Objects;

public class HostFactoryHostForm extends JPanel {
  private PropertyChangeListener listener;
  private JList<String> tokensList;
  private final JTextField hostName = new JTextField();

  public HostFactoryHostForm(List<String> tokens) {
    initializeComponents(tokens);
  }

  public void setPropertyChangeListener(PropertyChangeListener listener) {
    this.listener = listener;
  }

  public String getHostName() {
    return hostName.getText().trim();
  }

  public String getSelectedToken() {
    return tokensList.getSelectedValue();
  }

  private void initializeComponents(List<String>  tokens) {
    setLayout(new GridBagLayout());

    add(new JLabel("Host Name:"),
      new GridBagConstraints(
      0, 0, 1, 1, 0, 0,
          GridBagConstraints.NORTHWEST,
          GridBagConstraints.NONE,
        new Insets(0,0,0,32), 0, 0
      )
    );

    hostName.setToolTipText("<html>Identifier of the Host to be created.<br>" +
        "It will be created within the account of the Host Factory.</html>");
    hostName.getDocument().addDocumentListener(new DefaultDocumentListener(e ->
        fireEvent("host.name", getHostName())));
    add(hostName,
      new GridBagConstraints(
          1, 0, 1, 1, 1, 0,
          GridBagConstraints.CENTER,
          GridBagConstraints.HORIZONTAL,
          new Insets(0,0,0,2), 0, 0
      )
    );
    add(Box.createVerticalStrut(8),
        new GridBagConstraints(
            0, 1, 1, 1, 0, 0,
            GridBagConstraints.CENTER,
            GridBagConstraints.NONE,
            new Insets(0,0,0,0), 0, 0
        )
    );
    add(new JLabel("Token:"),
      new GridBagConstraints(
        0, 2, 1, 1, 0, 0,
        GridBagConstraints.NORTHWEST,
        GridBagConstraints.NONE,
        new Insets(0,0,0,48), 0, 0
      )
    );

    DefaultListModel<String> tokensModel = new DefaultListModel<>();
    tokens.forEach(tokensModel::addElement);

    tokensList = new JList<>(tokensModel);
    tokensList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    tokensList.addListSelectionListener(e -> fireEvent("selected.token", getSelectedToken()));

    add(new JScrollPane(tokensList),
      new GridBagConstraints(
          1, 2, 1, 1, 1, 1,
          GridBagConstraints.NORTHWEST,
          GridBagConstraints.BOTH,
          new Insets(0,4,0,6), 0, 0
      )
    );
  }

  private void fireEvent(String name, String value) {
    if (Objects.nonNull(listener)) {
      listener.propertyChange(
          new PropertyChangeEvent(this, name, null, value));
    }
  }
}
