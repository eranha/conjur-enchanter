package com.cyberark.actions.resource;

import com.cyberark.actions.ActionType;

public interface ResourceAction {
  ActionType getActionType();
  boolean isSelectionBased();
}
