package com.cyberark.views;

import com.cyberark.Application;
import com.cyberark.components.PolicyForm;
import com.cyberark.components.ResourceForm;
import com.cyberark.dialogs.InputDialog;
import com.cyberark.exceptions.ResourceAccessException;
import com.cyberark.models.PolicyModel;
import com.cyberark.models.ResourceIdentifier;
import com.cyberark.models.ResourceType;
import com.cyberark.resource.ResourceServiceFactory;
import com.cyberark.resource.ResourcesService;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ViewFactory {
  private static ViewFactory instance;
  private MessageView messageView;

  private ViewFactory() {
  }

  private ResourcesService getResourcesService() {
    return ResourceServiceFactory.getInstance().getResourcesService();
  }

  public static ViewFactory getInstance() {
    if (instance == null) {
      instance = new ViewFactory();
    }
    return instance;
  }

  public PolicyFormView getPolicyView() throws ResourceAccessException {
    return new PolicyForm(getResourcesService().getPolicies());
  }

  public MessageView getMessageView() {
    if (messageView == null) {
      return new MessageView() {
        @Override
        public void showMessageDialog(String message) {
          JOptionPane.showMessageDialog(Application.getInstance().getMainForm(), message);
        }

        @Override
        public void showMessageDialog(Object message, String title, int messageType) {
          JOptionPane.showMessageDialog(Application.getInstance().getMainForm(), message, title, messageType);
        }
      };
    }

    return messageView  ;
  }

  public void setMessaegView(MessageView messaegView) {
    this.messageView = messaegView;
  }
}
