package com.cyberark.views;

import com.cyberark.actions.resource.EditSecretAction;
import com.cyberark.actions.resource.RotateSecretAction;
import com.cyberark.models.*;
import com.cyberark.models.table.ResourceTableModel;
import com.cyberark.models.table.SecretTableModel;

import javax.swing.*;
import java.util.List;

import static com.cyberark.util.Resources.getString;

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
    items.add(new EditSecretAction(this::getSelectedResource, getString("edit.secret.action.text")));
    items.add(new RotateSecretAction(this::getSelectedResource, getString("rotate.secret.action.text")));
    return items;
  }

  @Override
  protected List<Action> getActions() {
    List<Action> items = super.getActions();
    items.add(new EditSecretAction(this::getSelectedResource));
    return items;
  }
}
