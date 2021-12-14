package com.cyberark.models.table;

import com.cyberark.models.Permission;

import javax.swing.table.AbstractTableModel;

public class PermissionsTableModel extends AbstractTableModel {
  Permission[] permissions;
  String[] columnNames = new String[]{"role", "privilege", "policy"};

  public PermissionsTableModel(Permission[] permissions) {
    this.permissions = permissions;
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
    switch (columnIndex) {
      case 0:
        return permissions[rowIndex].role;
      case 1:
        return permissions[rowIndex].privilege;
      case 2:
        return permissions[rowIndex].policy;
    }
    return null;
  }
}
