package com.cyberark.models.audit;

import com.cyberark.models.ResourceIdentifier;

import javax.swing.table.AbstractTableModel;
import java.util.List;

import static com.cyberark.util.Resources.getString;

public class AuditTableModel extends AbstractTableModel {
  private final List<AuditEvent> events;
  private final String[] columns = getString("audit.table.model.columns").split(",");

  private final Class<?>[] columnClass = new Class<?>[] {
      String.class, ResourceIdentifier.class, String.class
  };

  public AuditTableModel(List<AuditEvent> events) {
    this.events = events;
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    return columnClass[columnIndex];
  }

  @Override
  public int getRowCount() {
    return events.size();
  }

  @Override
  public int getColumnCount() {
    return columns.length;
  }

  @Override
  public String getColumnName(int column) {
    return columns[column];
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    AuditEvent event = events.get(rowIndex);
    switch (columnIndex) {
      case 0:
        return event.timestamp;
      case 1:
          return event.getUser();
      case 2:
        return event.getMessage();
    }
    return null;
  }

  public List<AuditEvent> getEvents() {
    return events;
  }

}
