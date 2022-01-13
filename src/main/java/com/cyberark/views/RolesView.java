package com.cyberark.views;

import com.cyberark.actions.resource.EditRoleAction;
import com.cyberark.actions.resource.RotateApiKeyAction;
import com.cyberark.actions.resource.UpdatePasswordAction;
import com.cyberark.models.table.ResourceTableModel;
import com.cyberark.models.RoleModel;
import com.cyberark.models.table.RoleTableModel;

import javax.swing.*;
import java.util.List;

import static com.cyberark.util.Resources.getString;

/**
 * Role resource is either user or host
 */
public class RolesView extends ResourceViewImpl<RoleModel> {
  public RolesView(ViewType view) {
    super(view);
  }

  @Override
  protected List<Action> getActions() {
    List<Action> actions = super.getActions();
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
    actions.add(new EditRoleAction(this::getSelectedResource, getString("edit.role.menu.action.text")));
    actions.add(new RotateApiKeyAction(this::getSelectedResource, getString("rotate.api.key.action.text")));
    actions.add(new UpdatePasswordAction(this::getSelectedResource, getString("update.password.menu.action.text")));
    return actions;
  }
}
