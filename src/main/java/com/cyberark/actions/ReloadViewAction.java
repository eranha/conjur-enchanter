package com.cyberark.actions;

import com.cyberark.controllers.ControllerFactory;
import com.cyberark.views.Icons;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import static com.cyberark.Consts.CYBR_BLUE;
import static com.cyberark.util.Resources.getString;

public class ReloadViewAction extends AbstractAction {
  public ReloadViewAction() {
    super(
      getString("reload.view.action.text"),
      Icons.getInstance().getIcon(Icons.ICON_SPIN,
        16,
        CYBR_BLUE
      )
    );
    putValue(SHORT_DESCRIPTION, getString("reload.view.action.description"));
    putValue(MNEMONIC_KEY, KeyEvent.VK_R);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    ControllerFactory.getInstance().getViewController().reloadView();
  }
}
