package com.cyberark.models.table;

import com.cyberark.Util;
import com.cyberark.models.ResourceIdentifier;
import com.cyberark.models.ResourceModel;

import javax.swing.table.AbstractTableModel;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.IntStream;

public class DefaultResourceTableModel<T extends ResourceModel> extends AbstractTableModel
    implements ResourceTableModel<T> {
  private final List<T> model;
  private final String[] columnNames =  {"ID", "Owner", "Policy", "Created"};
  private String[][] grid;

  public DefaultResourceTableModel(List<T> model) {
    this.model = model;
    grid = new String[model.size()][];

    populateTableModel(model);
  }

  private void populateTableModel(List<T> model) {
    for (int i = 0; i < model.size(); i++) {
      ResourceModel row = model.get(i);
      ResourceIdentifier id = row.getIdentifier();
      grid[i] = new String[] {
          id.getId(),
          Util.stringIsNotNullOrEmpty(row.getOwner())
              ? ResourceIdentifier.fromString(row.getOwner()).getId()
              : null,
          Util.stringIsNotNullOrEmpty(row.getPolicy())
              ? ResourceIdentifier.fromString(row.getPolicy()).getId()
              : null,
          row.getCreatedAt()
      };
    }
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
    if (columnIndex == 3) {
      return Util.prettyDate(grid[rowIndex][columnIndex]);
    }
    return (model != null && grid != null) ? grid[rowIndex][columnIndex] : null;
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

  public T getItemAt(int index) {
    return model.get(index);
  }

  @Override
  public int getResourceModelIndex(final ResourceModel resourceModel) {
    if (model != null) {
      OptionalInt indexOpt = IntStream.range(0, model.size())
          .filter(i -> resourceModel.getId().equals(model.get(i).getId()))
          .findFirst();
      return indexOpt.orElse(-1);
    }

    return -1;
  }

  @Override
  public void clearData() {
    if (model != null) {
      model.clear();
      grid = null;
      fireTableDataChanged();
    }
  }

  protected List<T> getModel() {
    return model;
  }
}
