package com.cyberark.components;

import com.cyberark.models.hostfactory.HostFactoryHostModel;
import com.cyberark.models.hostfactory.HostFactoryToken;
import com.cyberark.models.table.AbstractEditableTableModel;
import com.cyberark.models.table.TokensTableModel;
import com.cyberark.views.Icons;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;

import static com.cyberark.Consts.DARK_BG;

public class HostFactoryHostForm extends ContainerBase {
  private static final Icon INFO_ICON = Icons.getInstance().getIcon(Icons.ICON_INFO, 16, DARK_BG);
  private PropertyChangeListener listener;
  private final HostFactoryTokensTable tokensTable;
  private final AnnotationsTable annotationsTable;
  private final JTextField hostName;
  private final static String INFO_TEXT = getString("host-factory.host.form.info.label");
  private final JLabel infoLabel = new JLabel(INFO_TEXT);

  public HostFactoryHostForm(HostFactoryToken[] tokens, String hostName) {
    this.hostName = new RequiredTextField(hostName);

    tokensTable = new HostFactoryTokensTable(
        e -> fireEvent(e.getPropertyName(), String.valueOf(e.getNewValue())),
        new TokensTableModel(tokens));
    annotationsTable = new AnnotationsTable(AbstractEditableTableModel.EditMode.AddRemove);
    initializeComponents();
  }

  public HostFactoryHostModel getModel() {
    return new HostFactoryHostModel(
        getHostName(),
        Objects.requireNonNull(tokensTable.getSelectedToken()),
        annotationsTable.getModel().getAnnotations());
  }

  public void setPropertyChangeListener(PropertyChangeListener listener) {
    this.listener = listener;
  }

  private String getHostName() {
    return hostName.getText().trim();
  }

  private void initializeComponents() {
    setLayout(new GridBagLayout());
    setPreferredSize(new Dimension(420, 240));
    infoLabel.setIcon(INFO_ICON);

    add(new JLabel(getString("host-factory.host.form.id.label")),
      new GridBagConstraints(
      0, 0, 1, 1, 0, 0,
          GridBagConstraints.NORTHWEST,
          GridBagConstraints.NONE,
        new Insets(0,0,0,32), 0, 0
      )
    );

    hostName.setToolTipText(getString("host-factory.host.form.tooltip"));
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
    add(new JLabel(getString("host-factory.host.form.tokens.label")),
      new GridBagConstraints(
        0, 2, 1, 1, 0, 0,
        GridBagConstraints.NORTHWEST,
        GridBagConstraints.NONE,
        new Insets(0,0,0,48), 0, 0
      )
    );

    tokensTable.setToolTipText(getString("host-factory.host.form.info.label"));

    add(new JScrollPane(tokensTable),
      new GridBagConstraints(
          1, 2, 1, 1, 1, 1,
          GridBagConstraints.NORTHWEST,
          GridBagConstraints.BOTH,
          new Insets(0,4,0,6), 0, 0
      )
    );

    add(infoLabel,
        new GridBagConstraints(
            1, 3, 1, 1, 0, 0,
            GridBagConstraints.WEST,
            GridBagConstraints.NONE,
            new Insets(2,4,4,0), 0, 0
        )
    );

    add(Box.createVerticalStrut(4),
        new GridBagConstraints(
            0, 4, 1, 1, 0, 0,
            GridBagConstraints.CENTER,
            GridBagConstraints.NONE,
            new Insets(0,0,0,0), 0, 0
        )
    );

    add(new JLabel(getString("host-factory.host.form.annotations.label")),
        new GridBagConstraints(
            0, 5, 1, 1, 0, 0,
            GridBagConstraints.NORTHWEST,
            GridBagConstraints.NONE,
            new Insets(0,0,0,48), 0, 0
        )
    );

    annotationsTable.setToolTipText(getString("host-factory.host.form.annotations.tooltip"));
    add(annotationsTable,
        new GridBagConstraints(
            1, 5, 1, 1, 1, 1,
            GridBagConstraints.NORTHWEST,
            GridBagConstraints.BOTH,
            new Insets(0,4,0,6), 0, 0
        )
    );
  }

  private void fireEvent(String name, String value) {
    if (tokensTable.getSelectedToken() == null) {
      infoLabel.setForeground(UIManager.getColor("Label.foreground"));
      infoLabel.setIcon(INFO_ICON);
    } else {
      infoLabel.setForeground(UIManager.getColor("Panel.background"));
      infoLabel.setIcon(null);
    }


    if (Objects.nonNull(listener)) {
      listener.propertyChange(
          new PropertyChangeEvent(this, name, null, value));
    }
  }
}
