package com.cyberark.components;

import com.cyberark.models.HostFactoryToken;
import org.ocpsoft.prettytime.PrettyTime;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.time.Instant;
import java.util.Date;

public class TokensTableCellRenderer extends DefaultTableCellRenderer {
  private HostFactoryToken[] tokens;

  public TokensTableCellRenderer(HostFactoryToken[] tokens) {
    this.tokens = tokens;
  }

  public TokensTableCellRenderer() {
  }

  @Override
  public Component getTableCellRendererComponent(JTable table,
                                                 Object value,
                                                 boolean isSelected,
                                                 boolean hasFocus,
                                                 int row,
                                                 int column) {
    Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

    if (parseDate(tokens[row].expiration).before(new Date(System.currentTimeMillis()))) {
      comp.setForeground(Color.lightGray);
    } else {
      comp.setForeground(isSelected
          ? UIManager.getColor("Table.selectionForeground")
          : UIManager.getColor("Table.foreground"));
    }

    if (column == 1 && comp instanceof JLabel) {
      ((JLabel) comp).setText(getFormattedDate(value.toString()));
    }

    return comp;
  }


  private static String getFormattedDate(String dateTime) {
    PrettyTime pt = new PrettyTime();
    return pt.format(parseDate(dateTime));
  }

  private static Date parseDate(String str) {
    return Date.from(Instant.parse( str ));
  }

  public void setTokens(HostFactoryToken[] tokens) {
    this.tokens = tokens;
  }
}
