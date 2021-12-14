package com.cyberark.models.table;

import com.cyberark.models.Privilege;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PrivilegesTableModel extends AbstractEditableTableModel<Privilege> {
  private List<Privilege> privileges;
  private final String[] columnNames = new String[]{"Permissions for %s", "Allow", "Deny"};
  private String role;

  public static final Map<String, Boolean> VARIABLE_PRIVILEGES =
      Arrays.stream(new String[]{"read", "execute", "update"})
      .collect(Collectors.toMap(privilege -> privilege, data -> false));

  public static final Map<String, Boolean> RESOURCE_PRIVILEGES =
      Arrays.stream(new String[]{"read", "create", "update"})
          .collect(Collectors.toMap(privilege -> privilege, data -> false));


  public PrivilegesTableModel(String role, Map<String, Boolean> permissions) {
    setModelData(role, permissions);
  }

  public void setModelData(String role, Map<String, Boolean> permissions) {
    this.role = role;
    this.privileges = permissions
        .entrySet()
        .stream()
        .map(e -> new Privilege(e.getKey(), e.getValue()))
        .collect(Collectors.toList());

    fireTableDataChanged();
  }

  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    return true;
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    Class<?> clazz = String.class;
    switch (columnIndex) {
      case 0:
        clazz = String.class;
        break;
      case 1:
      case 2:
        clazz = Boolean.class;
        break;
    }
    return clazz;
  }

  @Override
  public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    Privilege p = privileges.get(rowIndex);
    switch (columnIndex) {
      case 0:
        p.setPrivilege(aValue.toString());
        fireTableDataChanged();
        break;
      case 1:
        p.setAllow(Boolean.parseBoolean(aValue.toString()));
        fireTableDataChanged();
        break;
      case 2:
        p.setAllow(!Boolean.parseBoolean(aValue.toString()));
        fireTableDataChanged();
        break;
    }
  }

  @Override
  public int getRowCount() {
    return privileges != null ? privileges.size() : 0;
  }

  @Override
  public String getColumnName(int column) {
    return column == 0 ? getPermissionsColumnName() : columnNames[column];
  }

  private String getPermissionsColumnName() {
    return role == null ? "Permissions": String.format(columnNames[0], role);
  }

  @Override
  public int getColumnCount() {
    return columnNames.length;
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    switch (columnIndex) {
      case 0:
        return privileges.get(rowIndex).getPrivilege();
      case 1:
        return privileges.get(rowIndex).isAllow();
      case 2:
        return !privileges.get(rowIndex).isAllow();
    }
    return null;
  }

  @Override
  public void addRow(Privilege row) {
    privileges.add(row);
    fireTableDataChanged();
  }

  @Override
  public void removeRow(int rowIndex) {
    privileges.remove(rowIndex);
    fireTableDataChanged();
  }

  public List<Privilege> getPrivileges() {
    return privileges;
  }
}
