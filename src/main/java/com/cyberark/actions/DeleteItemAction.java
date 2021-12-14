package com.cyberark.actions;


import com.cyberark.exceptions.ResourceAccessException;
import com.cyberark.models.ResourceModel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.function.Supplier;

public class DeleteItemAction<T extends ResourceModel> extends ActionBase<T> {

  public DeleteItemAction(Supplier<T> selectedResource) {
    this(selectedResource, "Delete");
  }

  public DeleteItemAction(Supplier<T> selectedResource, String text) {
    super(text, ActionType.DeleteItem, selectedResource);
    putValue(SHORT_DESCRIPTION, "Delete the selected resource");
    putValue(MNEMONIC_KEY, KeyEvent.VK_D);
    putValue(Action.ACCELERATOR_KEY,
        KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, InputEvent.META_DOWN_MASK));
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
        showErrorDialog(ex.getMessage());
      }
    }
  }
}
