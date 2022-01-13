package com.cyberark.actions.resource;

import com.cyberark.Application;
import com.cyberark.Consts;
import com.cyberark.Util;
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
  public static final String POLICY_PUT_LOAD_WARNING = "<html>Note: In <b>PUT</b> policy load mode, " +
      "objects, grants, and privileges that <br>exist in the " +
      "database but are not specified in the policy file are " +
      "<span style='background-color: yellow'>deleted</span>.<br><br>" +
      "Are you sure you want to continue?</html>";
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
    ViewFactory viewFactory = ViewFactory.getInstance();

    try {
      PolicyFormView view = viewFactory.getPolicyView();

      while (true) {
        if (view.showDialog(
            Application.getInstance().getMainForm(),
            String.format("%s - Load Policy", APP_NAME), () -> Util.nonNullOrEmptyString(view.getPolicyText())
        ) == InputDialog.OK_OPTION) {
          String policyText = view.getPolicyText();
          String policyBranch = view.getBranch();

          if (view.getPolicyApiMode() == Consts.PolicyApiMode.Put) {
            Object[] options = {"Yes",
                "No", "Cancel"};
            int selection = JOptionPane.showOptionDialog(getMainForm(),
                new JLabel(POLICY_PUT_LOAD_WARNING),
                "Confirm Operation",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE, null, options, options[2]);

            if (selection == JOptionPane.CANCEL_OPTION) {
              view.setPolicyText(policyText);
              view.setBranch(policyBranch);
              continue; // user cancelled, show the form again
            } else if (selection != JOptionPane.YES_OPTION) {
              break;
            }
          }

          loadPolicy(viewFactory, view, policyText, policyBranch);
        }

        break;
      }
    } catch (Throwable ex) {
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

  private void loadPolicy(ViewFactory viewFactory, PolicyFormView view, String policyText, String policyBranch) throws ResourceAccessException {
    String response;
    if (policyText != null && policyText.trim().length() > 0) {
      response = getResourcesService().loadPolicy(
          view.getPolicyApiMode(),
          policyText,
          policyBranch == null
            ? Consts.ROOT_POLICY
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

  private void promptToCopyApiKey(String response) {
    messageView.showMessageDialog(
        new ApiKeysView(response),
    "Policy Load Response",
          JOptionPane.INFORMATION_MESSAGE
    );
  }
}
