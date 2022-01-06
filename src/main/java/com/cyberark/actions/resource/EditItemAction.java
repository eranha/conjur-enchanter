package com.cyberark.actions.resource;

import com.cyberark.actions.ActionType;
import com.cyberark.models.ResourceModel;

import java.awt.event.KeyEvent;
import java.util.function.Supplier;

@SelectionBasedAction
public abstract class EditItemAction<T extends ResourceModel> extends ActionBase<T> {
  public EditItemAction(Supplier<T> selectedResource) {
    this(selectedResource, "Edit");
  }

  public EditItemAction(Supplier<T> selectedResource, String text) {
    super(text, ActionType.EditItem, selectedResource);
    putValue(SHORT_DESCRIPTION, "Edit the selected resource");
    putValue(MNEMONIC_KEY, KeyEvent.VK_E);
    setEnabled(false);
  }
}