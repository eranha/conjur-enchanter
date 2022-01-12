package com.cyberark.actions.resource.hostfactory;

import com.cyberark.actions.ActionType;
import com.cyberark.actions.resource.SelectionBasedAction;
import com.cyberark.components.HostFactoryTokensTable;
import com.cyberark.dialogs.InputDialog;
import com.cyberark.exceptions.ResourceAccessException;
import com.cyberark.models.hostfactory.HostFactory;
import com.cyberark.models.hostfactory.HostFactoryToken;
import com.cyberark.models.table.TokensTableModel;
import com.cyberark.views.ViewFactory;

import javax.swing.*;
import java.awt.*;
import java.time.Instant;
import java.util.Arrays;
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

    if (Arrays.stream(model.getTokens())
        .noneMatch(
            t -> Instant.parse( t.getExpiration() ).isAfter(Instant.now()))
    ) {
      ViewFactory.getInstance().getMessageView().showMessageDialog(
          "All tokens have expired."
      );
      return;
    }

    HostFactoryTokensTable tokensTable = new HostFactoryTokensTable(
        e -> {
          Window ancestor = SwingUtilities.getWindowAncestor((Component) e.getSource());
          if (ancestor instanceof JDialog) {
            ((JDialog)ancestor).getRootPane().getDefaultButton().setEnabled(e.getNewValue() != null);
          }
        },
        new TokensTableModel(model.getTokens()));

    tokensTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

    if (InputDialog.showDialog(getMainForm(),
          "Host Factory - Select Token",
          true,
          new JScrollPane(tokensTable),
          false,
          InputDialog.OK_CANCEL_OPTION)
        == InputDialog.OK_OPTION) {

      List<HostFactoryToken> tokens = tokensTable.getSelectedTokens();

      if (tokens != null && tokens.size() > 0) {
        String message = tokens.size() > 1
              ? String.format("Are you sure you want to delete %s selected tokens?", tokens.size())
              : String.format("<html>Are you sure you want to delete the selected token <br>token: '%s'," +
                "<br>expiration:%s ?</html>",
            tokens.get(0).getToken(), tokens.get(0).getExpiration());

        if (JOptionPane.showConfirmDialog(getMainForm(), new JLabel(message)) == JOptionPane.YES_OPTION) {
          try {
            for (HostFactoryToken token : tokens) {
              getResourcesService().revokeHostFactoryToken(token.getToken());
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
