package com.cyberark.components;

import com.cyberark.models.ResourceIdentifier;
import com.cyberark.views.Icons;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

import static com.cyberark.Consts.CYBR_BLUE;

public class RoleTableCellRenderer extends DefaultTableCellRenderer {

  public RoleTableCellRenderer() {
  }

  @Override
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                 boolean hasFocus, int row, int column) {
    Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

    if (value instanceof ResourceIdentifier && c instanceof JLabel) {
      JLabel label = (JLabel)c;
      ResourceIdentifier role = (ResourceIdentifier) value;
      label.setIcon(Icons.getInstance().getIcon(role.getType(), 16, isSelected ? Color.WHITE : CYBR_BLUE));
      label.setText(role.getId());
    }

    return c;
  }
}
