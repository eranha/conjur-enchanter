package com.cyberark.components;

import com.cyberark.models.hostfactory.HostFactoryToken;
import com.cyberark.models.table.TokensTableModel;
import org.ocpsoft.prettytime.PrettyTime;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
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

  public String getToolTipText(MouseEvent e) {
    String tip = null;
    java.awt.Point p = e.getPoint();
    int rowIndex = rowAtPoint(p);
    int colIndex = columnAtPoint(p);


    try {
      String value = getValueAt(rowIndex, colIndex).toString();
      tip = (colIndex == 1) ? getFormattedDate(value): value;
    } catch (RuntimeException e1) {
      //catch null pointer exception if mouse is over an empty line
    }

    return tip;
  }

  private static String getFormattedDate(String dateTime) {
    return (new PrettyTime()).format(Date.from(Instant.parse(dateTime)));
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
