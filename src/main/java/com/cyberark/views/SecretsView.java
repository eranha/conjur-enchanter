package com.cyberark.views;

import com.cyberark.actions.resource.EditSecretAction;
import com.cyberark.actions.resource.RotateSecretAction;
import com.cyberark.models.*;
import com.cyberark.models.table.ResourceTableModel;
import com.cyberark.models.table.SecretTableModel;

import javax.swing.*;
import java.util.List;

public class SecretsView extends ResourceViewImpl<SecretModel> {
  public SecretsView() {
    super(ViewType.Secrets);
  }


  @Override
  protected ResourceTableModel<SecretModel> createTableModel(List<SecretModel> items) {
    return new SecretTableModel(items);
  }

  @Override
  protected List<Action> getMenuActions() {
    List<Action> items = super.getMenuActions();
    items.add(new EditSecretAction(this::getSelectedResource, "Set Secret..."));
    items.add(new RotateSecretAction(this::getSelectedResource, "Rotate Secret..."));
    return items;
  }

  @Override
  protected List<Action> getActions() {
    List<Action> items = super.getActions();
    items.add(new EditSecretAction(this::getSelectedResource));
    return items;
  }
}
