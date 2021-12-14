package com.cyberark.models.table;

import javax.swing.table.AbstractTableModel;

public abstract class AbstractEditableTableModel<T> extends AbstractTableModel {
  public abstract void addRow(T row);
  public abstract void removeRow(int rowIndex);

}
