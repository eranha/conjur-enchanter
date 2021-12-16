package com.cyberark.actions;

import com.cyberark.Util;
import com.cyberark.components.PrivilegesPanel;
import com.cyberark.dialogs.InputDialog;
import com.cyberark.event.EventPublisher;
import com.cyberark.event.ResourceEvent;
import com.cyberark.exceptions.ResourceAccessException;
import com.cyberark.models.ResourceIdentifier;
import com.cyberark.models.ResourceModel;
import com.cyberark.models.ResourceType;

import java.util.*;
import java.util.function.Supplier;

@SelectionBasedAction
public class EditPermissions<T extends ResourceModel> extends ActionBase<T> {
  public EditPermissions(Supplier<T> selectedResource) {
    super("Edit Permissions...", ActionType.EditPermissions, selectedResource);
    setEnabled(false);
  }

  @Override
  public void actionPerformed(ResourceModel resource) {
    PrivilegesPanel privilegesPane = new PrivilegesPanel(
        resource.getIdentifier().getType(),
        resource.permissions,
        getResources()
    );

    if (InputDialog.showDialog(
        getMainForm(),
        String.format("Edit Permissions of %s", ResourceIdentifier.fromString(resource.id).getId()),
        true,
        privilegesPane) == InputDialog.OK_OPTION) {
      Map<ResourceIdentifier, Set<String>> updatedPrivileges = privilegesPane.getResourcePrivileges();
      HashMap<ResourceIdentifier, Set<String>> deniedPrivileges = new HashMap<>();
      HashMap<ResourceIdentifier, Set<String>> permittedMap = new HashMap<>();

      Arrays.stream(resource.permissions).forEach(permission -> {
        ResourceIdentifier role = ResourceIdentifier.fromString(permission.role);
        if (!updatedPrivileges.containsKey(role) ||
            updatedPrivileges
                .get(role)
                .stream()
                .noneMatch(p -> p.equals(permission.privilege))) {
          deniedPrivileges.computeIfAbsent(role, v -> new HashSet<>());
          deniedPrivileges.get(role).add(permission.privilege);
        }
      });

      // Map resource permission: role -> privileges
      HashMap<ResourceIdentifier, Set<String>> resourcePermissions = new HashMap<>();
      Arrays.stream(resource.permissions).forEach(p -> {
            ResourceIdentifier id = ResourceIdentifier.fromString(p.role);
            resourcePermissions.computeIfAbsent(id, v -> new HashSet<>());
            resourcePermissions.get(id).add(p.privilege);
          }
      );

      updatedPrivileges.forEach((role, listOfPrivileges) -> listOfPrivileges.forEach(p -> {
        if (!resourcePermissions.containsKey(role) || !resourcePermissions.get(role).contains(p)) {
          permittedMap.computeIfAbsent(role, v -> new HashSet<>());
          permittedMap.get(role).add(p);
        }
      }));
      System.out.printf("denyList: %s\n", deniedPrivileges);
      System.out.printf("permitList: %s\n", permittedMap);
      try {
        if (!deniedPrivileges.isEmpty()){
          getResourcesService().deny(resource, deniedPrivileges);
        }
        if (!permittedMap.isEmpty()) {
          getResourcesService().permit(resource, permittedMap);
        }
        EventPublisher.getInstance().fireEvent(new ResourceEvent<>(resource));
      } catch (ResourceAccessException ex) {
        showErrorDialog(ex.getMessage());
      }
    }// @Dialog approved
  }

  private List<ResourceIdentifier> getResources() {
    ResourceType resourceType = getSelectedRresourceType();

    try {
      if (Util.isRoleResource(resourceType)) {
        return getResourcesService().getResourceIdentifiers();
      } else {
        return getResourcesService().getResourceIdentifiers(Util::isRoleResource);
      }
    } catch (ResourceAccessException e) {
      showErrorDialog(e.getMessage());
    }

    return new ArrayList<>();
  }

  private ResourceType getSelectedRresourceType() {
    return getSelectedResource().getIdentifier().getType();
  }
}
