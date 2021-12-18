package com.cyberark.actions;

import com.cyberark.Application;
import com.cyberark.Consts;
import com.cyberark.components.ApiKeysView;
import com.cyberark.dialogs.InputDialog;
import com.cyberark.event.EventPublisher;
import com.cyberark.event.Events;
import com.cyberark.exceptions.ApiCallException;
import com.cyberark.exceptions.ResourceAccessException;
import com.cyberark.models.ResourceType;
import com.cyberark.resource.ResourcesService;
import com.cyberark.views.ErrorView;
import com.cyberark.views.MessageView;
import com.cyberark.views.PolicyFormView;
import com.cyberark.views.ViewFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import static com.cyberark.Consts.APP_NAME;

public class NewPolicyAction extends NewResourceAction {
  private final MessageView messageView;

  public NewPolicyAction(ResourcesService resourcesService,
                         MessageView messageView) {
    super(
        ResourceType.policy,
        KeyEvent.VK_P,
        KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_DOWN_MASK)
    );

    this.messageView = messageView;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    showPolicyForm();
  }

  private void showPolicyForm() {
    String response;
    ViewFactory viewFactory = ViewFactory.getInstance();

    try {
      PolicyFormView view = viewFactory.getPolicyView();

      if (view.showDialog(Application.getInstance().getMainForm(),
          String.format("%s - Load Policy", APP_NAME)) == InputDialog.OK_OPTION) {
        String policyText = view.getPolicyText();
        String policyBranch = view.getBranch();

        if (policyText != null && policyText.trim().length() > 0) {
          response = getResourcesService().loadPolicy(
              view.getPolicyApiMode(),
              policyText,
              policyBranch == null
                ? Consts.ROOT
                : policyBranch
          );

          if (response.contains("api_key")) {
            promptToCopyApiKey(response);
          } else {
            viewFactory.getMessageView().showMessageDialog(response);
          }

          EventPublisher.getInstance().fireEvent(
              new ActionEvent(
                  this,
                  Events.NEW_ITEM,
                  ResourceType.policy.toString())
          );
        }
      }
    } catch (ResourceAccessException ex) {
      ex.printStackTrace();

      if (ex.getCause() instanceof ApiCallException) {
        ErrorView.showApiCallErrorMessage((ApiCallException) ex.getCause());
      } else {
        ErrorView.showErrorMessage(
            String.format(
              "Error loading policy: %s",
              ex.getMessage()
          )
        );
      }
    }
  }

  private void promptToCopyApiKey(String response) {
    messageView.showMessageDialog(
        new ApiKeysView(response),
    "Policy Load Response",
          JOptionPane.INFORMATION_MESSAGE
    );
  }
}
