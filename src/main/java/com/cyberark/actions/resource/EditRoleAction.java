package com.cyberark.actions.resource;

import com.cyberark.Util;
import com.cyberark.components.Form;
import com.cyberark.components.ItemsSelector;
import com.cyberark.dialogs.InputDialog;
import com.cyberark.exceptions.ResourceAccessException;
import com.cyberark.models.Membership;
import com.cyberark.models.ResourceIdentifier;
import com.cyberark.models.ResourceType;
import com.cyberark.models.RoleModel;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Adds an actor role either user or host as a member of one or more groups or layers respectively.
 */
@SelectionBasedAction
public class EditRoleAction extends EditItemAction<RoleModel> {
  public EditRoleAction(Supplier<RoleModel> selectedResource) {
    this(selectedResource, getString("edit.role.action.text"));
  }

  public EditRoleAction(Supplier<RoleModel> selectedResource, String text) {
    super(selectedResource, text);
    putValue(SHORT_DESCRIPTION, getString("edit.role.action.short.description"));
  }

  @Override
  public void actionPerformed(RoleModel roleModel) {
    try {
      ResourceIdentifier role = roleModel.getIdentifier();

      // Get all the possible granting roles, must be either group or layer.
      List<ResourceIdentifier> grantingRoles = getResourcesService().getResourceIdentifiers(
          t -> role.getType() == ResourceType.user
            ? t == ResourceType.group
            : t == ResourceType.layer);

      // Get the role memberships
      List<Membership> memberships;

      try {
        memberships = getMembership(role);
      } catch (ResourceAccessException ex) {
        return; // called method in super class displays the error
      }

      List<ResourceIdentifier> grantedRoles = new ArrayList<>();

      Map<String, ResourceIdentifier> grantedRolesSetsModels = grantingRoles.stream()
          .collect(Collectors.toMap(ResourceIdentifier::getId, v -> v));

      // remove all the granted roles from the granting roles list
      memberships.forEach(i -> {
        String id = ResourceIdentifier.fromString(i.getRole()).getId();
        grantingRoles.remove(grantedRolesSetsModels.get(id));
        grantedRoles.add(grantedRolesSetsModels.get(id));
      });

      Set<String> membershipSet = memberships
          .stream()
          .map(Membership::getRole)
          .collect(Collectors.toSet());

      ItemsSelector itemsSelector = new ItemsSelector(
          grantingRoles, grantedRoles
      );

      String title = String.format(getString("edit.role.action.form.title"),
          Util.resourceTypeToTitle(role.getType()),
          (role.getType() == ResourceType.user
              ? getString("edit.role.action.form.title.groups")
              : getString("edit.role.action.form.title.layers")));
      Form form = new Form(
          title,
          getResourcesInfo().getProperty("role.members"),
          itemsSelector
      );

      itemsSelector.setPreferredSize(new Dimension(500, 360));

      if (InputDialog.showModalDialog(
          getMainForm(),
          title,
          form) == InputDialog.OK_OPTION) {
        List<ResourceIdentifier> selectedItems = itemsSelector.getSelectedItems();
        List<ResourceIdentifier> unSelectedItems = itemsSelector.getUnSelectedItems();

        List<ResourceIdentifier> revokeSet = unSelectedItems
            .stream()
            .filter(i -> membershipSet.contains(i.getFullyQualifiedId())).collect(Collectors.toList());

        if (!revokeSet.isEmpty()) {
          getResourcesService().revoke(roleModel, revokeSet);
        }

        List<ResourceIdentifier> grantSet = selectedItems
            .stream()
            .filter(i -> !membershipSet.contains(i.getFullyQualifiedId()))
            .collect(Collectors.toList());

        if (!grantSet.isEmpty()) {
          getResourcesService().grant(roleModel, grantSet);
        }
      }
    } catch (Exception ex) {
      showErrorDialog(ex);
    }
  }
}

