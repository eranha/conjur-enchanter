package com.cyberark.models.table;

import com.cyberark.Util;
import com.cyberark.models.SecretModel;

import java.util.List;

public class SecretTableModel extends DefaultResourceTableModel<SecretModel> {

  public SecretTableModel(List<SecretModel> secretModels) {
    super(secretModels);
  }

  @Override
  public int getRowCount() {
    return getModel().size();
  }

  @Override
  public String getColumnName(int column) {

    return column < 4
        ? super.getColumnName(column)
        : "secret";
  }

  @Override
  public int getColumnCount() {
    return super.getColumnCount() + 1;
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    SecretModel model = getModel().get(rowIndex);

    if (columnIndex == 4) {
      return Util.maskSecret(model.getSecret());
    }

    return super.getValueAt(rowIndex, columnIndex);
  }
}
