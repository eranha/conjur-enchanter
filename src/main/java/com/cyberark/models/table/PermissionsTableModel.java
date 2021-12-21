package com.cyberark.models.table;

import com.cyberark.models.Permission;
import com.cyberark.models.ResourceIdentifier;

import javax.swing.table.AbstractTableModel;

public class PermissionsTableModel extends AbstractTableModel {
  private Permission[] permissions;
  private final String[] columnNames = new String[]{"role", "privilege"};
  private final Class<?>[] columnClass = new Class<?>[] {
     ResourceIdentifier.class, String.class
  };

  public PermissionsTableModel(Permission[] permissions) {
    this.permissions = permissions;
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    return columnClass[columnIndex];
  }

  @Override
  public int getRowCount() {
    return permissions.length;
  }

  @Override
  public String getColumnName(int column) {
    return columnNames[column];
  }

  @Override
  public int getColumnCount() {
    return columnNames.length;
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    return columnIndex == 0
        ? ResourceIdentifier.fromString(permissions[rowIndex].getRole())
        : permissions[rowIndex].getPrivilege();
  }
}
