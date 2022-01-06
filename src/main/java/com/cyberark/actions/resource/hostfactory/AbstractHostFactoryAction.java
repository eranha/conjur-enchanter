package com.cyberark.actions.resource.hostfactory;

import com.cyberark.actions.ActionType;
import com.cyberark.actions.resource.ActionBase;
import com.cyberark.models.hostfactory.HostFactory;
import com.cyberark.models.hostfactory.HostFactoryToken;

import javax.swing.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Supplier;

public abstract class AbstractHostFactoryAction extends ActionBase<HostFactory> {
   public AbstractHostFactoryAction(
      ActionType actionType,
      Supplier<HostFactory> selectedResource,
      String text) {
    super(text, actionType, selectedResource);
    setEnabled(false);
  }

  protected static HashMap<Integer, String> getErrorCodeMapping() {
    HashMap<Integer, String> errors = new HashMap<>();
    errors.put(422, "error");
    errors.put(401, "host_factory");
    return errors;
  }

  protected JList<String> getTokensList(HostFactory model) {
    JList<String> list = new JList<>();
    DefaultListModel<String> tokens = new DefaultListModel<>();
    Arrays.stream(model.getTokens()).map(HostFactoryToken::getToken).forEach(tokens::addElement);

    list.setModel(tokens);
    list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    return list;
  }
}
