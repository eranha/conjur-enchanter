package com.cyberark.actions;

import com.cyberark.controllers.ControllerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class ReloadViewAction extends AbstractAction {
  public ReloadViewAction() {
    super("Reload");
    putValue(SHORT_DESCRIPTION, "Reload view");
    putValue(MNEMONIC_KEY, KeyEvent.VK_R);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    ControllerFactory.getInstance().getViewController().reloadView();
  }
}
