package com.cyberark.actions;

import com.cyberark.Util;
import com.cyberark.components.Form;
import com.cyberark.components.ItemsSelector;
import com.cyberark.dialogs.InputDialog;
import com.cyberark.exceptions.ResourceAccessException;
import com.cyberark.models.Membership;
import com.cyberark.models.ResourceIdentifier;
import com.cyberark.models.ResourceModel;
import com.cyberark.models.ResourceType;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * A SetResource is either a group or a layer in the system.
 * This acitons adds a role (group, layer, user or host) as members
 * to the current selected group or layer.
 */
@SelectionBasedAction
public class EditSetResourceAction extends EditItemAction<ResourceModel> {
  public EditSetResourceAction(Supplier<ResourceModel> selectedResource) {
    super(selectedResource);
  }

  public EditSetResourceAction(Supplier<ResourceModel> selectedResource, String text) {
    super(selectedResource, text);
  }

  @Override
  protected void actionPerformed(ResourceModel resource) {
    if (resource.getIdentifier().getType() != ResourceType.layer &&
        resource.getIdentifier().getType() != ResourceType.group) {
      throw new IllegalStateException(resource.getIdentifier().getType().toString());
    }

    try {
      // The selected role either layer or group
      ResourceIdentifier role = resource.getIdentifier();

      // Get all the possible roles that can be granted, must be
      // either group/user for a group or layer/host for layer.
      List<ResourceIdentifier> roles = getResourcesService().getResourceIdentifiers(
          t -> role.getType() == ResourceType.group
            ? t == ResourceType.user || t == ResourceType.group
            : t == ResourceType.host || t == ResourceType.layer);

      // remove the current slected role from the roles list
      roles.remove(
          roles.stream()
              .filter(i-> i.equals(role))
              .findFirst()
              .orElse(null));

      Map<String, ResourceIdentifier> rolesMap = roles.stream()
          .collect(Collectors.toMap(ResourceIdentifier::getId, v -> v));

      // Get the set role members
      List<Membership> members;

      try {
        members = getMembers(role);
      } catch (ResourceAccessException ex) {
        return; // called method in super class display the error message
      }

      List<ResourceIdentifier> grantedRoles = new ArrayList<>();

      
      // Remove all member roles from roles list
      members.forEach(i -> {
        String id = ResourceIdentifier.fromString(i.getMember()).getId();
        roles.remove(rolesMap.get(id));

        // adminn user is not returned in the genral roles list
        if (rolesMap.containsKey(id)) {
          grantedRoles.add(rolesMap.get(id));
        }
      });
      
      Set<String> membersSet = members.stream().map(Membership::getMember).collect(Collectors.toSet());

      ItemsSelector itemsSelector = new ItemsSelector(
          roles, grantedRoles);

      itemsSelector.setPreferredSize(new Dimension(500, 360));

      Form form = new Form(
          String.format("Add %s to %s",
              role.getType() == ResourceType.group ? "Users" : "Hosts",
              Util.resourceTypeToTitle(role.getType())),
          getResourcesInfo().getProperty("role.members"),
          itemsSelector
      );


      if (InputDialog.showDialog(
          getMainForm(),
          String.format(
              "Add %s to %s: %s",
              role.getType() == ResourceType.group ? "Users" : "Hosts",
              Util.resourceTypeToTitle(role.getType()), role.getId()
          ),
          true,
          form) == InputDialog.OK_OPTION) {
        List<ResourceIdentifier> selectedItems = itemsSelector.getSelectedItems();
        List<ResourceIdentifier> unSelectedItems = itemsSelector.getUnSelectedItems();

        // Evaluate if one or more roles has been revoked from this group/layer
        List<ResourceIdentifier> removedMembers = unSelectedItems
            .stream()
            .filter(i -> membersSet.contains(i.getFullyQualifiedId()))
            .collect(Collectors.toList());

        if (!removedMembers.isEmpty()) {
          getResourcesService().revoke(removedMembers, role);
        }

        // Evaluate if one or more new roles was granted to this group/layer
        List<ResourceIdentifier> newMembers = selectedItems
            .stream()
            .filter(i -> !membersSet.contains(i.getFullyQualifiedId())).collect(Collectors.toList());

        if (!newMembers.isEmpty()) {
          getResourcesService().grant(newMembers, role);
        }
      }
    } catch (Exception ex) {
      showErrorDialog(ex);
    }
  }
}
