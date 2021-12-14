package com.cyberark.actions;

public interface ResourceAction {
  ActionType getActionType();
  boolean isSelectionBased();
}
