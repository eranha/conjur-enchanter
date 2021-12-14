package com.cyberark.views;

import com.cyberark.actions.EditRoleAction;
import com.cyberark.actions.RotateApiKeyAction;
import com.cyberark.models.table.ResourceTableModel;
import com.cyberark.models.RoleModel;
import com.cyberark.models.table.RoleTableModel;

import javax.swing.*;
import java.util.List;

/**
 * Role resouerce is either user or host
 */
public class RolesView extends ResourceViewImpl<RoleModel> {
  public RolesView(ViewType view) {
    super(view);
  }

  @Override
  protected List<Action> getActions() {
    List<Action> actions = super.getActions();
    //actions.add(new NewRoleAction(this));
    actions.add(new EditRoleAction(this::getSelectedResource));
    return actions;
  }

  @Override
  protected ResourceTableModel<RoleModel> createTableModel(List<RoleModel> items) {
    return new RoleTableModel(items);
  }

  @Override
  protected List<Action> getMenuActions() {
    List<Action> actions = super.getMenuActions();
    //actions.add(new NewRoleAction(this, "New..."));
    actions.add(new EditRoleAction(this::getSelectedResource, "Edit Memberships..."));
    actions.add(new RotateApiKeyAction(this::getSelectedResource));
    return actions;
  }
}
