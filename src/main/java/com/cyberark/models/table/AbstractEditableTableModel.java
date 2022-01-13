package com.cyberark.models.table;

import com.cyberark.util.Resources;

import javax.swing.table.AbstractTableModel;

public abstract class AbstractEditableTableModel<T> extends AbstractTableModel {
  public abstract void addRow(T row);
  public abstract void removeRow(int rowIndex);


  protected static String getString(String key) {
    return Resources.getString(key);
  }

  public enum EditMode {
    AddRemove,
    AddOnly,
    ReadOnly
  }

  public abstract EditMode getEditMode();
}
