package com.cyberark.actions.resource.hostfactory;

import com.cyberark.actions.ActionType;
import com.cyberark.actions.resource.SelectionBasedAction;
import com.cyberark.components.HostFactoryTokensForm;
import com.cyberark.components.JsonViewer;
import com.cyberark.dialogs.InputDialog;
import com.cyberark.exceptions.ResourceAccessException;
import com.cyberark.models.hostfactory.HostFactory;
import com.cyberark.models.hostfactory.HostFactoryTokensFormModel;

import java.util.function.Supplier;

@SelectionBasedAction
public class CreateTokensAction extends AbstractHostFactoryAction {

  public CreateTokensAction(Supplier<HostFactory> selectedResource) {
    super(
        ActionType.CreateTokens,
        selectedResource,
        getString("create.tokens.action.text")
    );
  }

  @Override
  protected void actionPerformed(HostFactory model) {
    final HostFactoryTokensForm tokensForm = new HostFactoryTokensForm(model.getId());
    final String formTitle = getString("create.tokens.action.form.title");
    final String resultTitle = getString("create.tokens.action.form.result.title");

    if (InputDialog.showModalDialog(
          getMainForm(),
          formTitle,
          tokensForm) == InputDialog.OK_OPTION) {
      HostFactoryTokensFormModel tokensFormModel = tokensForm.getModel();

      if (tokensFormModel.getExpirationDuration() == 0) {
        // set 1 day by default
        tokensFormModel.setExpirationDays(1);
      }

      try {
        JsonViewer.showDialog(
            getMainForm(),
            resultTitle,
            getResourcesService().createHostFactoryTokens(tokensFormModel));
        fireEvent(model);
      } catch (ResourceAccessException e) {
        showErrorDialog(e, getErrorCodeMapping());
      }
    }
  }
}
