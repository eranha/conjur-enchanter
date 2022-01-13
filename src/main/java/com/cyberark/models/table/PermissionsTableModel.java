package com.cyberark.models.table;

import com.cyberark.models.Permission;
import com.cyberark.models.ResourceIdentifier;

import javax.swing.table.AbstractTableModel;

import static com.cyberark.util.Resources.getString;

public class PermissionsTableModel extends AbstractTableModel {
  private Permission[] permissions;
  private final String[] columnNames = getString("permissions.table.model.columns").split(",");
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
