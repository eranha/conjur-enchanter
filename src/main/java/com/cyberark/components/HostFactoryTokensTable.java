package com.cyberark.components;

import com.cyberark.models.hostfactory.HostFactoryToken;
import com.cyberark.models.table.TokensTableModel;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class HostFactoryTokensTable extends JTable {
  private final PropertyChangeListener listener;
  private final TokensTableModel model;

  public HostFactoryTokensTable(PropertyChangeListener listener, TokensTableModel model) {
    super(model);
    this.listener = listener;
    this.model = model;
    initializeComponents();
  }

  private void initializeComponents() {
    setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    setDefaultRenderer(String.class, new TokensTableCellRenderer());
    getSelectionModel().addListSelectionListener(e -> fireEvent("selected.token",
        getSelectedRow() > -1 ? Objects.requireNonNull(getSelectedToken()).getToken() : null));
  }

  private void fireEvent(String name, String token) {
    if (Objects.nonNull(listener)) {
      listener.propertyChange(
          new PropertyChangeEvent(this, name, null, token));
    }
  }

  public HostFactoryToken getSelectedToken() {
    return getSelectedRow() > -1 ? model.getToken(getSelectedRow()) : null;
  }

  @Override
  public void setModel(TableModel dataModel) {
    if (dataModel instanceof TokensTableModel) {
      super.setModel(dataModel);
    } else {
      throw new IllegalArgumentException("dataModel");
    }
  }

  public List<HostFactoryToken> getSelectedTokens() {
    return Arrays.stream(getSelectedRows())
        .mapToObj(model::getToken)
        .collect(Collectors.toList());
  }
}
