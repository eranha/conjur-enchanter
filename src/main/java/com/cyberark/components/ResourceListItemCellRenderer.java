package com.cyberark.components;

import com.cyberark.models.ResourceIdentifier;
import com.cyberark.models.ResourceModel;
import com.cyberark.views.Icons;

import javax.swing.*;
import java.awt.*;

import static com.cyberark.Consts.DARK_BG;

public class ResourceListItemCellRenderer extends DefaultListCellRenderer {
  public Component getListCellRendererComponent(
      JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
    ResourceIdentifier resource = null;

    if (value == null) {
      throw new IllegalArgumentException("value is null");
    }

    setText(value.toString());

    if (value instanceof ResourceModel) {
      ResourceModel model = (ResourceModel) value;
      resource = ResourceIdentifier.fromString(model.id);
    } else if (value instanceof ResourceIdentifier) {
      resource = (ResourceIdentifier) value;
    }

    if (resource != null ) {
      if ( !(resource.getId().equals("Inherit")) ) {
        setIcon(Icons.getInstance().getIcon(resource.getType(), 16, isSelected ? Color.WHITE : DARK_BG));
      }

      setText(resource.getId());
    }

    return this;
  }
}
