package com.cyberark.models.table;

import javax.swing.event.TableModelEvent;
import java.util.ArrayList;

public class StringTableModel extends AbstractEditableTableModel<String> {

  private final ArrayList<String> rows = new ArrayList<>();

  @Override
  public void addRow(String row) {
    rows.add(row);
    fireTableChanged(new TableModelEvent(this));
  }

  public boolean isCellEditable(int row, int col) {
    return true;
  }

  @Override
  public void setValueAt(Object value, int row, int col) {
    if (value != null && value.toString().trim().length() > 0) {
      rows.set(row, value.toString().trim());
    }
    fireTableChanged(new TableModelEvent(this));
  }

  @Override
  public void removeRow(int rowIndex) {
    rows.remove(rowIndex);
    fireTableChanged(new TableModelEvent(this, rowIndex));
  }

  @Override
  public EditMode getEditMode() {
    return EditMode.AddRemove;
  }

  @Override
  public int getRowCount() {
    return rows.size();
  }

  @Override
  public int getColumnCount() {
    return 1;
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    return rows.get(rowIndex);
  }

  public String[] getItems() {
    return rows.toArray(new String[0]);
  }
}
