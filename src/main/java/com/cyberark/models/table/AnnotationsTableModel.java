package com.cyberark.models.table;

import com.cyberark.models.Annotation;

import javax.swing.event.TableModelEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class AnnotationsTableModel extends AbstractEditableTableModel<Annotation> {
  private final ArrayList<Annotation> annotations ;
  private final String[] columnNames = new String[]{"name", "value"};
  private final EditMode editMode;

  public AnnotationsTableModel(EditMode editMode) {
    this(editMode, new Annotation[0]);
  }

  public AnnotationsTableModel(Annotation[] annotations) {
    this(EditMode.ReadOnly, annotations);
  }

  public AnnotationsTableModel(EditMode editMode, Annotation[] annotations) {
    this.editMode = editMode;
    this.annotations = Arrays.stream(annotations).collect(Collectors.toCollection(ArrayList::new));
  }

  public Annotation[] getAnnotations() {
    return annotations.toArray(new Annotation[0]);
  }



  @Override
  public String getColumnName(int column) {
    return columnNames[column];
  }

  public boolean isCellEditable(int row, int col) {
    return editMode != EditMode.ReadOnly;
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
    return  (columnIndex == 0)
        ? annotations.get(rowIndex).getName()
        : annotations.get(rowIndex).getValue();
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

  @Override
  public EditMode getEditMode() {
    return editMode;
  }
}
