package com.cyberark.models.table;

import com.cyberark.models.HostFactoryToken;

import javax.swing.table.AbstractTableModel;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

public class TokensTableModel extends AbstractTableModel {

  private HostFactoryToken[] tokens = new HostFactoryToken[0];
  private final String[] columnNames  = new String[]{"ID", "Expiration", "CIDR"};

  public TokensTableModel() {
  }

  public TokensTableModel(HostFactoryToken[] tokens) {
    setTokens(tokens);
  }

  @Override
  public String getColumnName(int column) {
    return columnNames[column];
  }

  @Override
  public int getRowCount() {
    return tokens.length;
  }

  @Override
  public int getColumnCount() {
    return 3;
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    return String.class;
  }

  @Override
  public boolean isCellEditable(int row, int column) {
    //all cells false
    return false;
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    HostFactoryToken token = tokens[rowIndex];
    switch (columnIndex) {
      case 0:
        return token.token;
      case 1:
        return token.expiration;
      case 2:
        return token.cidr.length > 0 ? Arrays.toString(token.cidr) : null;
    }
    return null;
  }

  public void setTokens(HostFactoryToken[] tokens) {
    List<HostFactoryToken> list = Arrays.asList(tokens);
    list.sort((x, y) -> Instant.parse(y.expiration).compareTo(Instant.parse(x.expiration)));
    this.tokens = list.toArray(HostFactoryToken[]::new);
    fireTableDataChanged();
  }
}
