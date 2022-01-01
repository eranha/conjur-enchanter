package com.cyberark.actions.hostfactory;

import com.cyberark.actions.ActionBase;
import com.cyberark.actions.ActionType;
import com.cyberark.actions.SelectionBasedAction;
import com.cyberark.models.HostFactory;

import javax.swing.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public abstract class AbstractHostFactoryAction extends ActionBase<HostFactory> {
  private final static Map<Integer, String> errorCodes = getErrorCodeMapping();

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
    Arrays.stream(model.getTokens()).map(t -> t.token).forEach(tokens::addElement);

    list.setModel(tokens);
    list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    return list;
  }
}
