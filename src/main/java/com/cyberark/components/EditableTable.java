package com.cyberark.components;

import com.cyberark.models.table.AbstractEditableTableModel;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.*;
import java.util.function.Function;

public class EditableTable<T> extends JPanel {
  private JTable table;
  private final AbstractEditableTableModel<T> model;

  public EditableTable(AbstractEditableTableModel<T> model,
                       Function<TableModel, T> newRowSupplier) {
    this(model, newRowSupplier, true);
  }

  public EditableTable(AbstractEditableTableModel<T> model,
                       Function<TableModel, T> newRowSupplier,
                       boolean showTableHeader) {
    this.model = model;
    initializeComponents(newRowSupplier);

    if (!showTableHeader) {
      table.setTableHeader(null);
    }
  }

  private void initializeComponents(Function<TableModel, T> newRowSupplier) {
    table = new JTable();
    JPanel tablePanel = new JPanel(new BorderLayout());
    JPanel tableControlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT,0,4));
    JButton addRowButton = new JButton("+");
    JButton removeRowButton = new JButton("-");
    JScrollPane scrollPane = new JScrollPane(table);

    setLayout(new BorderLayout());
    table.setModel(model);
    addRowButton.setPreferredSize(new Dimension(16,16));
    removeRowButton.setPreferredSize(new Dimension(16,16));
    tableControlPanel.add(addRowButton);
    tableControlPanel.add( Box.createRigidArea(new Dimension(4, 6)));
    tableControlPanel.add(removeRowButton);

    removeRowButton.setEnabled(false);

    removeRowButton.addActionListener(e -> model.removeRow(table.getSelectedRow()));

    table.getSelectionModel().addListSelectionListener(e -> removeRowButton.setEnabled(table.getSelectedRow() > -1));

    addRowButton.addActionListener(e -> {
      getModel().addRow(newRowSupplier.apply(model));
      if (getModel().getRowCount() > 0) {
        table.setRowSelectionInterval(getModel().getRowCount() - 1, getModel().getRowCount() - 1);
      }
    });

    tablePanel.add(tableControlPanel, BorderLayout.SOUTH);
    tablePanel.add(scrollPane, BorderLayout.CENTER);
    add(tablePanel, BorderLayout.CENTER);
  }

  public AbstractEditableTableModel<T> getModel() {
    return model;
  }

  public JTable getTable() {
    return table;
  }
}
