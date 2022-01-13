package com.cyberark.actions.resource;

import com.cyberark.Util;
import com.cyberark.actions.ActionType;
import com.cyberark.components.ResourcePolicyView;
import com.cyberark.dialogs.InputDialog;
import com.cyberark.exceptions.ResourceAccessException;
import com.cyberark.models.Membership;
import com.cyberark.models.ResourceIdentifier;
import com.cyberark.models.ResourceModel;
import com.cyberark.models.ResourceType;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@SelectionBasedAction
public class ViewResourcePolicyAction<T extends ResourceModel> extends ActionBase<T> {
  public ViewResourcePolicyAction(Supplier<T> selectedResource) {
    this(selectedResource, getString("view.resource.policy.action.text"));
  }

  public ViewResourcePolicyAction(Supplier<T> selectedResource, String text) {
    super(text, ActionType.ViewPolicy, selectedResource);
    putValue(SHORT_DESCRIPTION, getString("view.resource.policy.action.description"));
    putValue(MNEMONIC_KEY, KeyEvent.VK_E);
    setEnabled(false);
  }

  @Override
  public void actionPerformed(ResourceModel resource) {
    String policy;
    List<Membership> membership = new ArrayList<>();
    List<Membership> members = new ArrayList<>();
    ResourceType type = resource.getIdentifier().getType();

    if (Util.isRoleResource(type)) {
      try {
        members = getMembers(resource.getIdentifier());
        membership = getMembership(resource.getIdentifier());
      } catch (ResourceAccessException ex) {
        return; // called method in super class displays the error
      }
    }

    List<ResourceIdentifier> grantedRoles = membership.stream()
        .map(i -> ResourceIdentifier.fromString(i.getRole())).collect(Collectors.toList());

    List<ResourceIdentifier> memberRoles = members.stream()
        .map(i -> ResourceIdentifier.fromString(i.getMember())).collect(Collectors.toList());

    policy = ResourceUtil.getResourcePolicy(resource);

    String permissions = ResourceUtil.getResourcePermissions(
        resource.getIdentifier(),
        memberRoles,
        grantedRoles
    );

    InputDialog.showDialog(getMainForm(),
        String.format(getString("view.resource.policy.action.dialog.title"),
            getString("application.name"),
            resource.getIdentifier().getId()),
         true,
        new ResourcePolicyView(
            resource,
            policy,
            permissions), InputDialog.OK_OPTION);
  }
}
