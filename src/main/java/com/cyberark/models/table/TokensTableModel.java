package com.cyberark.models.table;

import com.cyberark.models.hostfactory.HostFactoryToken;

import javax.swing.table.AbstractTableModel;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static com.cyberark.util.Resources.getString;

public class TokensTableModel extends AbstractTableModel {

  private HostFactoryToken[] tokens = new HostFactoryToken[0];
  private final String[] columnNames  = getString("tokens.table.model.columns").split(",");

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
        return token.getToken();
      case 1:
        return token.getExpiration();
      case 2:
        return token.getCidr().length > 0 ? Arrays.toString(token.getCidr()) : null;
    }
    return null;
  }

  public void setTokens(HostFactoryToken[] tokens) {
    List<HostFactoryToken> list = Arrays.asList(tokens);
    list.sort((x, y) -> Instant.parse(y.getExpiration()).compareTo(Instant.parse(x.getExpiration())));
    this.tokens = list.toArray(HostFactoryToken[]::new);
    fireTableDataChanged();
  }

  public HostFactoryToken getToken(int row) {
    return tokens[row];
  }
}
