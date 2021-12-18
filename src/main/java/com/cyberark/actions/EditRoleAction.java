package com.cyberark.actions;

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
 * Adds an actor role either user or host as a meber of one or more groups or layers respectivly.
 */
@SelectionBasedAction
public class EditRoleAction extends EditItemAction<RoleModel> {
  public EditRoleAction(Supplier<RoleModel> selectedResource) {
    this(selectedResource, "Edit");
  }

  public EditRoleAction(Supplier<RoleModel> selectedResource, String text) {
    super(selectedResource, text);
  }

  @Override
  public void actionPerformed(RoleModel roleModel) {
    try {
      ResourceIdentifier resourceIdentifier = roleModel.getIdentifier();

      // Get all the possible granting roles, must be either group or layer.
      List<ResourceIdentifier> grantingRoles = getResourcesService().getResourceIdentifiers(
          t -> resourceIdentifier.getType() == ResourceType.user
            ? t == ResourceType.group
            : t == ResourceType.layer);

      // Get the resourceIdentifier memberships
      List<Membership> memberships = null;

      try {
        memberships = getMembership(resourceIdentifier);
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

      itemsSelector.setPreferredSize(new Dimension(500, 360));

      if (InputDialog.showDialog(
          getMainForm(),
          "Manage Access",
          true,
          itemsSelector) == InputDialog.OK_OPTION) {
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
