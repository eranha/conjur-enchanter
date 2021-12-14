package com.cyberark.models.table;

import com.cyberark.models.ResourceModel;

import javax.swing.table.AbstractTableModel;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.IntStream;

public class DefaultResourceTableModel<T extends ResourceModel> extends AbstractTableModel
    implements ResourceTableModel<T> {
  private final List<T> model;
  private final String[] columnNames =  {"id", "owner", "policy", "created_at"};

  public DefaultResourceTableModel(List<T> model) {
    this.model = model;
  }

  @Override
  public int getRowCount() {
    return (model != null) ? model.size() : 0;
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
    if (model != null) {

      ResourceModel row = model.get(rowIndex);

      switch (columnIndex) {
        case 0:
          return row.id;
        case 1:
          return row.owner;
        case 2:
          return row.policy;
        case 3:
          return row.created_at;
      }
    }
    return null;
  }

  @Override
  public List<T> getResourceModels() {
    return model;
  }

  @Override
  public T getResourceModel(int rowIndex) {
    if (rowIndex < 0 || rowIndex > model.size() - 1) {
      throw new ArrayIndexOutOfBoundsException(rowIndex);
    }

    return model.get(rowIndex);
  }

  @Override
  public int getResourceModelIndex(final ResourceModel resourceModel) {
    if (model != null) {
      OptionalInt indexOpt = IntStream.range(0, model.size())
          .filter(i -> resourceModel == model.get(i))
          .findFirst();
      return indexOpt.orElse(-1);
    }

    return -1;
  }

  @Override
  public void clearData() {
    if (model != null) {
      model.clear();
      fireTableDataChanged();
    }
  }

  protected List<T> getModel() {
    return model;
  }
}
