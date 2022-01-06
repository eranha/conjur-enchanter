package com.cyberark.actions.resource;


import com.cyberark.actions.ActionType;
import com.cyberark.exceptions.ResourceAccessException;
import com.cyberark.models.ResourceModel;
import com.cyberark.views.Icons;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.function.Supplier;

import static com.cyberark.Consts.CYBR_BLUE;
import static com.cyberark.Consts.DARK_BG;

@SelectionBasedAction
public class DeleteItemAction<T extends ResourceModel> extends ActionBase<T> {

  public DeleteItemAction(Supplier<T> selectedResource) {
    this(selectedResource, "Delete");
    putValue(SMALL_ICON, Icons.getInstance().getIcon(Icons.ICON_TRASH,
        16,
        CYBR_BLUE));
  }

  public DeleteItemAction(Supplier<T> selectedResource, String text) {
    super(text, ActionType.DeleteItem, selectedResource);
    putValue(SHORT_DESCRIPTION, "Delete the selected resource");
    putValue(MNEMONIC_KEY, KeyEvent.VK_D);
    putValue(Action.ACCELERATOR_KEY,
        KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, InputEvent.META_DOWN_MASK));
    putValue(SMALL_ICON, Icons.getInstance().getIcon(Icons.ICON_TRASH,
        16,
        DARK_BG));
    setEnabled(false);
  }

  @Override
  public void actionPerformed(ResourceModel resource) {
    String message = "Are you sure you want to delete the selected item?";
    if (JOptionPane.showConfirmDialog(getMainForm(), message) == JOptionPane.YES_OPTION) {
      try {
        getResourcesService().delete(resource);
        fireEvent(resource);
      } catch (ResourceAccessException ex) {
        showErrorDialog(ex);
      }
    }
  }
}
