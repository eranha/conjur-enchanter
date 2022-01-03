package com.cyberark.components;

import javax.swing.*;
import javax.swing.table.TableModel;

import static com.cyberark.Consts.DARK_BG;
import static com.cyberark.Consts.LABEL_FOREGROUND;

public class DataTable extends JTable {
  public  DataTable() {
    initializeComponents();
  }

  public DataTable(TableModel model) {
    this();
    setModel(model);
  }

  private void initializeComponents() {
    setShowGrid(true);
    getTableHeader().setBackground(DARK_BG);
    getTableHeader().setForeground(LABEL_FOREGROUND);
    getTableHeader().setOpaque(false);
  }
}
