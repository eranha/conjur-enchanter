package com.cyberark.actions;

import com.cyberark.Consts;
import com.cyberark.PolicyBuilder;
import com.cyberark.Util;
import com.cyberark.components.ResourcePolicyView;
import com.cyberark.dialogs.InputDialog;
import com.cyberark.exceptions.ResourceAccessException;
import com.cyberark.models.*;

import java.awt.event.KeyEvent;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@SelectionBasedAction
public class ViewResourcePolicyAction<T extends ResourceModel> extends ActionBase<T> {
  public ViewResourcePolicyAction(Supplier<T> selectedResource) {
    this(selectedResource, "View Policy");
  }

  public ViewResourcePolicyAction(Supplier<T> selectedResource, String text) {
    super(text, ActionType.ViewPolicy, selectedResource);
    putValue(SHORT_DESCRIPTION, "View the selected resource policy");
    putValue(MNEMONIC_KEY, KeyEvent.VK_E);
    setEnabled(false);
  }

  @Override
  public void actionPerformed(ResourceModel resource) {
    String policy = null;
    PolicyBuilder policyBuilder = new PolicyBuilder();
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

    if (resource.getIdentifier().getType() == ResourceType.policy) {
      PolicyModel model = (PolicyModel) resource;

      if (model.getPolicyVersions().length > 0) {
        // get the latest policy version
        Optional<PolicyVersion> policyVersion = Arrays.stream(model.getPolicyVersions())
            .max(Comparator.comparingInt(PolicyVersion::getVersion))
            .stream()
            .findFirst();
        policy = policyVersion.map(PolicyVersion::getPolicyText).orElse(null);
      }
    } else {
      policy = policyBuilder.resource(resource.getIdentifier())
          .annotations(resource.getAnnotations())
          .permissions(resource.getIdentifier(), resource.getPermissions())
      .toPolicy();
    }

    PolicyBuilder policyPermissionsBuilder = new PolicyBuilder();

    if (Util.isSetResource(type)) {
      // list members in a single grant
      policyPermissionsBuilder.grants(resource.getIdentifier(), memberRoles);
    }

    // for each role this resource is granted, create a single grant
    String permissions = policyPermissionsBuilder
        .grants(grantedRoles, resource.getIdentifier()).toPolicy();

    InputDialog.showDialog(getMainForm(),
        String.format("%s - %s - Resource Policy", Consts.APP_NAME, resource.getIdentifier().getId())
        , true, new ResourcePolicyView(resource,
            policy, permissions), InputDialog.OK_OPTION);
  }
}
