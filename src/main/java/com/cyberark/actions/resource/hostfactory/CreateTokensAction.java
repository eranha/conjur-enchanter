package com.cyberark.actions.resource.hostfactory;

import com.cyberark.Util;
import com.cyberark.actions.ActionType;
import com.cyberark.actions.resource.SelectionBasedAction;
import com.cyberark.components.HostFactoryTokensForm;
import com.cyberark.dialogs.InputDialog;
import com.cyberark.models.hostfactory.HostFactory;
import com.cyberark.models.hostfactory.HostFactoryTokensFormModel;

import javax.swing.*;
import java.util.function.Supplier;

@SelectionBasedAction
public class CreateTokensAction extends AbstractHostFactoryAction {

  public CreateTokensAction(Supplier<HostFactory> selectedResource) {
    super(ActionType.CreateTokens, selectedResource, "Create Tokens...");
  }

  @Override
  public void actionPerformed(HostFactory model) {

    HostFactoryTokensForm tokensForm = new HostFactoryTokensForm(model.getId());
    if (InputDialog.showDialog(
        getMainForm(),
        "Host Factory - Create Tokens",
        true,
        tokensForm) == InputDialog.OK_OPTION) {
      HostFactoryTokensFormModel tokensFormModel = tokensForm.getModel();

      if (tokensFormModel.getExpirationDuration() == 0) {
        // set 1 day by default
        tokensFormModel.setExpirationDays(1);
      }

      try {
        String response = getResourcesService().createHostFactoryTokens(tokensFormModel);
        String indented = Util.prettyPrintJson(response);
        JPanel panel = new JPanel();
        JTextArea jt = new JTextArea(indented);

        panel.setBorder(BorderFactory.createEmptyBorder(0,0,0,4));
        panel.add(new JScrollPane(jt));

        JOptionPane.showMessageDialog(getMainForm(),
            panel,
            "Host Factory - Create Tokens Result",
            JOptionPane.INFORMATION_MESSAGE
        );
        fireEvent(model);
      } catch (Exception e) {
        showErrorDialog(e, getErrorCodeMapping());
      }
    }
  }
}
