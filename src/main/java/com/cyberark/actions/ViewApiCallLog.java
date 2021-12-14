package com.cyberark.actions;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.function.Consumer;

public class ViewApiCallLog extends AbstractAction {
  private final Consumer<Boolean> logViewer;

  public ViewApiCallLog(Consumer<Boolean> logViewer) {
    super("View Log");
    this.logViewer = logViewer;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (getValue("isLogViewVisible") == null || getValue("isLogViewVisible") == Boolean.FALSE) {
      putValue(Action.NAME, "Hide Log");
      putValue("isLogViewVisible", Boolean.TRUE);
      logViewer.accept(true);
    } else {
      putValue(Action.NAME, "View Log");
      putValue("isLogViewVisible", Boolean.FALSE);
      logViewer.accept(false);
    }
  }
}
