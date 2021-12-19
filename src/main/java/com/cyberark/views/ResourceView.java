package com.cyberark.views;

import com.cyberark.actions.ActionType;
import com.cyberark.models.DataModel;

import javax.swing.*;
import java.util.function.Consumer;

/**
 * Interface to all resource based views.
 * Defines operations and events on a single resource level.
 */
public interface ResourceView extends View {
  Action getAction(ActionType type);
  void toggleSelectionBasedActions(boolean enabled);
  void setSelectionListener(Consumer<DataModel> consumer);
  void setTableRowDoubleClickedEventListener(Consumer<ResourceView> consumer);
}
