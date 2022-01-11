package com.cyberark.components;

import com.cyberark.models.table.TokensTableModel;
import org.ocpsoft.prettytime.PrettyTime;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.time.Instant;
import java.util.Date;

/**
 * The standard class for rendering (displaying) individual host factory tokens in cells
 * in a <code>JTable</code>.
 */
public class TokensTableCellRenderer extends DefaultTableCellRenderer {
  /**
   *
   * Returns the default table cell renderer.
   * Expired tokens are rendered in <code>Color.lightGray</code>
   *
   * @param table  the <code>JTable</code>
   * @param value  the value to assign to the cell at
   *                  <code>[row, column]</code>
   * @param isSelected true if cell is selected
   * @param hasFocus true if cell has focus
   * @param row  the row of the cell to render
   * @param column the column of the cell to render
   * @return the default table cell renderer
   * @see javax.swing.JComponent#isPaintingForPrint()
   */
  @Override
  public Component getTableCellRendererComponent(JTable table,
                                                 Object value,
                                                 boolean isSelected,
                                                 boolean hasFocus,
                                                 int row,
                                                 int column) {
    Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

    if (table.getModel() instanceof TokensTableModel) {
      TokensTableModel model = (TokensTableModel) table.getModel();

      if (parseDate(model.getToken(row).getExpiration()).before(new Date(System.currentTimeMillis()))) {
        comp.setForeground(Color.lightGray);
      } else {
        comp.setForeground(isSelected
            ? UIManager.getColor("Table.selectionForeground")
            : UIManager.getColor("Table.foreground"));
      }

      if (column == 1 && comp instanceof JLabel) {
        ((JLabel) comp).setText(getFormattedDate(value.toString()));
      }
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
}
