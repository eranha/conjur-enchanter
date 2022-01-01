package com.cyberark.actions.hostfactory;

import com.cyberark.actions.ActionType;
import com.cyberark.actions.SelectionBasedAction;
import com.cyberark.dialogs.InputDialog;
import com.cyberark.exceptions.ResourceAccessException;
import com.cyberark.models.HostFactory;
import com.cyberark.views.ViewFactory;

import javax.swing.*;
import java.util.List;
import java.util.function.Supplier;

@SelectionBasedAction
public class RevokeTokensAction extends AbstractHostFactoryAction {

  public RevokeTokensAction(Supplier<HostFactory> selectedResource) {
    super(ActionType.RevokeToken, selectedResource, "Revoke Tokens...");
  }

  @Override
  public void actionPerformed(HostFactory model) {
    if (model.getTokens() == null || model.getTokens().length == 0) {
      ViewFactory.getInstance().getMessageView().showMessageDialog(
          String.format("Host Factory '%s' has no tokens defined.", model.getIdentifier().getId())
      );
      return;
    }

    JList<String> list = getTokensList(model);
    list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

    if (InputDialog.showDialog(getMainForm(),
        "Host Factory - Select Token",
        true,
        new JScrollPane(list))
        == InputDialog.OK_OPTION) {
      List<String> tokens = list.getSelectedValuesList();

      if (tokens != null && tokens.size() > 0) {
        String message = tokens.size() > 1
            ? String.format("Are you sure you want to delete %s selected tokens?", tokens.size())
            : String.format("Are you sure you want to delete the selected token '%s'?", tokens.get(0));

        if (JOptionPane.showConfirmDialog(getMainForm(), message) == JOptionPane.YES_OPTION) {
          try {
            for (String token : tokens) {
              getResourcesService().revokeHostFactoryToken(token);
            }
            String msg = tokens.size() == 1
                ? String.format("Token '%s' has been revoked.", tokens.get(0))
                : String.format("%s tokens has been revoked.", tokens.size());
            ViewFactory.getInstance().getMessageView().showMessageDialog(
                msg
            );
            fireEvent(model);
          } catch (ResourceAccessException e) {
            showErrorDialog(e, getErrorCodeMapping());
          }
        }
      }
    }
  }
}
