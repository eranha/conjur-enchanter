package com.cyberark.components;

import com.cyberark.models.ResourceIdentifier;
import com.cyberark.views.Icons;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

import static com.cyberark.Consts.DARK_BG;

public class ResourceTreeTreeCellRenderer extends DefaultTreeCellRenderer {
  @Override
  public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                boolean selected, boolean expanded, boolean leaf, int row,
                                                boolean hasFocus) {
    Component c = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

    if (c instanceof JLabel) {
      JLabel label = (JLabel)c;
      if (((DefaultMutableTreeNode)value).getUserObject() instanceof ResourceIdentifier) {
        ResourceIdentifier id = (ResourceIdentifier) ((DefaultMutableTreeNode)value).getUserObject();
        label.setIcon(Icons.getInstance().getIcon(id.getType(), 16, DARK_BG));
        int index = id.getId().lastIndexOf('/');
        label.setText(id.getId());

        if (index > -1 && index + 1 < id.getId().length()) {
          label.setText(id.getId().substring(index + 1));
        }
      } else {
        label.setForeground(Color.red);
        label.setText(String.valueOf(value));
      }
    }
    return c;
  }
}
