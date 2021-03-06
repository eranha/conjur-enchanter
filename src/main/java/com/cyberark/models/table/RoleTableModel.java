package com.cyberark.models.table;

import com.cyberark.models.RoleModel;

import java.util.Arrays;
import java.util.List;

public class RoleTableModel extends DefaultResourceTableModel<RoleModel> {
  private final List<RoleModel> roleModels;


  public RoleTableModel(List<RoleModel> roleModels) {
    super(roleModels);
    this.roleModels = roleModels;
  }

  @Override
  public int getRowCount() {
    return roleModels.size();
  }

  @Override
  public String getColumnName(int column) {
    return column < 4
        ? super.getColumnName(column)
        : getString("role.table.model.restricted.column");
  }

  @Override
  public int getColumnCount() {
    return super.getColumnCount() + 1;
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    RoleModel model = roleModels.get(rowIndex);

    if (columnIndex == 4) {
      return model.getRestrictedTo().length > 0 ? Arrays.toString(model.getRestrictedTo()) : null;
    }
    return super.getValueAt(rowIndex, columnIndex);
  }
}
