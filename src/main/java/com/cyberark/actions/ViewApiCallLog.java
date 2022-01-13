package com.cyberark.actions;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.function.Consumer;

import static com.cyberark.util.Resources.getString;

public class ViewApiCallLog extends AbstractAction {
  private final Consumer<Boolean> logViewer;

  public ViewApiCallLog(Consumer<Boolean> logViewer) {
    super(getString("view.api.call.log.action.text"));
    this.logViewer = logViewer;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (getValue("isLogViewVisible") == null || getValue("isLogViewVisible") == Boolean.FALSE) {
      putValue(Action.NAME, getString("view.api.call.log.action.hide.text"));
      putValue("isLogViewVisible", Boolean.TRUE);
      logViewer.accept(true);
    } else {
      putValue(Action.NAME, getString("view.api.call.log.action.text"));
      putValue("isLogViewVisible", Boolean.FALSE);
      logViewer.accept(false);
    }
  }
}
