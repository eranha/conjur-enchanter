package com.cyberark.models.table;

import com.cyberark.models.Privilege;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PrivilegesTableModel extends AbstractEditableTableModel<Privilege> {
  private List<Privilege> privileges;
  private final String[] columnNames = new String[] {
      getString("privileges.pane.table.privilege.column.format"),
      getString("privileges.pane.table.allow.column"),
      getString("privileges.pane.table.deny.column")
  };
  private String role;

  public static final Map<String, Boolean> EXECUTE_PRIVILEGES =
      Arrays.stream(getString("privileges.pane.table.default.execute.privilege").split(","))
      .collect(Collectors.toMap(privilege -> privilege, data -> false));

  public static final Map<String, Boolean> CREATE_UPDATE_PRIVILEGES =
      Arrays.stream(getString("privileges.pane.table.default.read.privilege").split(","))
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
    return role == null
        ? getString("privileges.pane.table.privilege.column")
        : String.format(getString("privileges.pane.table.privilege.column.format"), role);
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

  @Override
  public EditMode getEditMode() {
    return EditMode.AddRemove;
  }

  public List<Privilege> getPrivileges() {
    return privileges;
  }
}
