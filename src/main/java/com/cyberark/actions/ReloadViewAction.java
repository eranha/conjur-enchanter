package com.cyberark.actions;

import com.cyberark.controllers.ControllerFactory;
import com.cyberark.views.Icons;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import static com.cyberark.Consts.CYBR_BLUE;

public class ReloadViewAction extends AbstractAction {
  public ReloadViewAction() {
    super("Reload", Icons.getInstance().getIcon(Icons.ICON_SPIN, 16, CYBR_BLUE));
    putValue(SHORT_DESCRIPTION, "Reload view");
    putValue(MNEMONIC_KEY, KeyEvent.VK_R);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    ControllerFactory.getInstance().getViewController().reloadView();
  }
}
