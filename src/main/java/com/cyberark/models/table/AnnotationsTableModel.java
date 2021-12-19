package com.cyberark.models.table;

import com.cyberark.models.Annotation;

import javax.swing.event.TableModelEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class AnnotationsTableModel extends AbstractEditableTableModel<Annotation> {
  private final boolean editable;
  ArrayList<Annotation> annotations = new ArrayList<>();
  String[] columnNames = new String[]{"name", "value", "policy"};

  public AnnotationsTableModel(Annotation[] annotations) {
    this(false);
    this.annotations = Arrays.stream(annotations).collect(Collectors.toCollection(ArrayList::new));
  }

  public Annotation[] getAnnotations() {
    return annotations.toArray(new Annotation[0]);
  }


  public AnnotationsTableModel(boolean editable) {
    this.editable = editable;
  }

  @Override
  public String getColumnName(int column) {
    return columnNames[column];
  }

  public boolean isCellEditable(int row, int col) {
    return editable;
  }

  public void setValueAt(Object value, int row, int col) {
    Annotation annotation = annotations.get(row);

    switch (col) {
      case 0:
        annotation.setName(value.toString());
        break;
      case 1:
        annotation.setValue(value.toString());
        break;
      case 2:
        annotation.setPolicy(value.toString());
        break;
    }

    fireTableCellUpdated(row, col);
  }


  @Override
  public int getRowCount() {
    return annotations.size();
  }


  @Override
  public int getColumnCount() {
    return columnNames.length;
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    switch (columnIndex) {
      case 0:
        return annotations.get(rowIndex).getName();
      case 1:
        return annotations.get(rowIndex).getValue();
      case 2:
        return annotations.get(rowIndex).getPolicy();
    }
    return null;
  }

  @Override
  public void addRow(Annotation annotation) {
    annotations.add(annotation);
    fireTableChanged(new TableModelEvent(this));
  }

  @Override
  public void removeRow(int selectedRow) {
    annotations.remove(selectedRow);
    fireTableChanged(new TableModelEvent(this));
  }
}
