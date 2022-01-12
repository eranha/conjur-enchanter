package com.cyberark.actions.resource;

import com.cyberark.Util;
import com.cyberark.actions.ActionType;
import com.cyberark.components.Form;
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
import java.util.stream.Collectors;

@SelectionBasedAction
public class EditPermissions<T extends ResourceModel> extends ActionBase<T> {
  public EditPermissions(Supplier<T> selectedResource) {
    super("Edit Permissions...", ActionType.EditPermissions, selectedResource);
    setEnabled(false);
  }

  @Override
  public void actionPerformed(ResourceModel resource) {
    ResourceType type = getSelectedResource().getIdentifier().getType();
    boolean isRole = Util.isRoleResource(type) || Util.isSetResource(type) || type == ResourceType.policy;


    PrivilegesPanel privilegesPane = new PrivilegesPanel(
        !(Util.isRoleResource(type) || Util.isSetResource(type))
            ? "Roles" : "Resources",
        resource.getIdentifier().getType(),
        resource.getPermissions(),
        getResources()
    );

    Form form = new Form(
        "Permissions",
        getResourcesInfo().getProperty("role.privileges"),
        privilegesPane
    );

    if (InputDialog.showDialog(
        getMainForm(),
        String.format("Edit Permissions of %s", resource.getIdentifier().getId()),
        true,
        form) == InputDialog.OK_OPTION) {

      Map<ResourceIdentifier, Set<String>> updatedPrivileges = privilegesPane.getResourcePrivileges();
      HashMap<ResourceIdentifier, Set<String>> deniedPrivileges = new HashMap<>();
      HashMap<ResourceIdentifier, Set<String>> permittedMap = new HashMap<>();

      Arrays.stream(resource.getPermissions())
        .forEach(permission -> {
          ResourceIdentifier role = ResourceIdentifier.fromString(permission.getRole());
          if (!updatedPrivileges.containsKey(role) ||
              updatedPrivileges
                  .get(role)
                  .stream()
                  .noneMatch(p -> p.equals(permission.getPrivilege()))) {
            deniedPrivileges.computeIfAbsent(role, v -> new HashSet<>());
            deniedPrivileges.get(role).add(permission.getPrivilege());
          }
        }
      );

      // Map resource permission: role -> privileges
      HashMap<ResourceIdentifier, Set<String>> resourcePermissions = new HashMap<>();
      Arrays.stream(resource.getPermissions())
        .forEach(p -> {
          ResourceIdentifier id = ResourceIdentifier.fromString(p.getRole());
          resourcePermissions.computeIfAbsent(id, v -> new HashSet<>());
          resourcePermissions.get(id).add(p.getPrivilege());
        }
      );

      updatedPrivileges
        .forEach((role, listOfPrivileges) -> listOfPrivileges.forEach(p -> {
            if (!resourcePermissions.containsKey(role) || !resourcePermissions.get(role).contains(p)) {
              permittedMap.computeIfAbsent(role, v -> new HashSet<>());
              permittedMap.get(role).add(p);
            }
          }
        )
      );


      try {

        if (!deniedPrivileges.isEmpty()){
          getResourcesService().deny(resource, deniedPrivileges);
        }

        if (!permittedMap.isEmpty()) {
          if (isRole) {
            getResourcesService().permit(resource, permittedMap);
          } else {
            getResourcesService().permit(permittedMap, resource);
          }
        }

        EventPublisher.getInstance().fireEvent(new ResourceEvent<>(resource));
      } catch (ResourceAccessException ex) {
        showErrorDialog(ex);
      }
    }// @Dialog approved
  }

  private List<ResourceIdentifier> getResources() {
    ResourceType resourceType = getSelectedResourceType();
    List<ResourceIdentifier> resources;
    try {
      if (Util.isRoleResource(resourceType)) {
        resources = getResourcesService().getResourceIdentifiers();
      } else {
        resources = getResourcesService().getResourceIdentifiers(Util::isRoleResource);
      }
    } catch (ResourceAccessException e) {
      showErrorDialog(e);
      return new ArrayList<>();
    }

    // filter out the current selected resource
    final String selectedResourceId = getSelectedResource().getIdentifier().getId();
    return resources
        .stream()
        .filter(r -> !(r.getId().equals(selectedResourceId)))
        .collect(Collectors.toList());
  }

  private ResourceType getSelectedResourceType() {
    return getSelectedResource().getIdentifier().getType();
  }
}
