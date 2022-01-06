package com.cyberark.components;

import com.cyberark.models.hostfactory.HostFactoryHostModel;
import com.cyberark.models.hostfactory.HostFactoryToken;
import com.cyberark.models.table.AbstractEditableTableModel;
import com.cyberark.models.table.TokensTableModel;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;

public class HostFactoryHostForm extends JPanel {
  private PropertyChangeListener listener;
  private final JTable tokensTable;
  private final AnnotationsTable annotationsTable;
  private final JTextField hostName;
  private final HostFactoryToken[] tokens;

  public HostFactoryHostForm(HostFactoryToken[] tokens, String hostName) {
    this.tokens = tokens;
    this.hostName = new RequiredTextField(hostName);

    tokensTable = new JTable();
    annotationsTable = new AnnotationsTable(AbstractEditableTableModel.EditMode.AddRemove);

    initializeComponents();
  }

  public HostFactoryHostModel getModel() {
    return new HostFactoryHostModel(
        getHostName(),
        Objects.requireNonNull(getSelectedToken()),
        annotationsTable.getModel().getAnnotations());
  }

  public void setPropertyChangeListener(PropertyChangeListener listener) {
    this.listener = listener;
  }

  private String getHostName() {
    return hostName.getText().trim();
  }

  private HostFactoryToken getSelectedToken() {
    return tokensTable.getSelectedRow() > -1
        ? tokens[tokensTable.getSelectedRow()]
        : null;
  }

  private void initializeComponents() {
    setLayout(new GridBagLayout());
    setPreferredSize(new Dimension(420, 240));

    add(new JLabel("Host name*"),
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
    add(Box.createVerticalStrut(12),
        new GridBagConstraints(
            0, 1, 1, 1, 0, 0,
            GridBagConstraints.CENTER,
            GridBagConstraints.NONE,
            new Insets(0,0,0,0), 0, 0
        )
    );
    add(new JLabel("Token*"),
      new GridBagConstraints(
        0, 2, 1, 1, 0, 0,
        GridBagConstraints.NORTHWEST,
        GridBagConstraints.NONE,
        new Insets(0,0,0,48), 0, 0
      )
    );


    tokensTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    tokensTable.setModel(new TokensTableModel(tokens));
    tokensTable.setDefaultRenderer(String.class, new TokensTableCellRenderer(tokens));
    tokensTable.getSelectionModel().addListSelectionListener(e -> fireEvent("selected.token",
        tokensTable.getSelectedRow() > -1 ? Objects.requireNonNull(getSelectedToken()).getToken() : null));

    add(new JScrollPane(tokensTable),
      new GridBagConstraints(
          1, 2, 1, 1, 1, 1,
          GridBagConstraints.NORTHWEST,
          GridBagConstraints.BOTH,
          new Insets(0,4,0,6), 0, 0
      )
    );

    add(Box.createVerticalStrut(12),
        new GridBagConstraints(
            0, 3, 1, 1, 0, 0,
            GridBagConstraints.CENTER,
            GridBagConstraints.NONE,
            new Insets(0,0,0,0), 0, 0
        )
    );

    add(new JLabel("Annotations"),
        new GridBagConstraints(
            0, 4, 1, 1, 0, 0,
            GridBagConstraints.NORTHWEST,
            GridBagConstraints.NONE,
            new Insets(0,0,0,48), 0, 0
        )
    );

    add(annotationsTable,
        new GridBagConstraints(
            1, 4, 1, 1, 1, 1,
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
