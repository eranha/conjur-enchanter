package com.cyberark.actions;

import com.cyberark.Application;
import com.cyberark.controllers.AuthnController;
import com.cyberark.controllers.ControllerFactory;
import com.cyberark.views.Icons;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import static com.cyberark.Consts.CYBR_BLUE;

public class LogoutAction extends AbstractAction {
  public LogoutAction() {
    super("Sign Out", Icons.getInstance().getIcon(Icons.LOCK_ICON_UNICODE, 16, CYBR_BLUE));
    putValue(SHORT_DESCRIPTION, "Sign Out");
    putValue(MNEMONIC_KEY, KeyEvent.VK_C);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    AuthnController authnController = ControllerFactory.getInstance().getAuthnController();
    authnController.logout();
    authnController.login();
  }
}
