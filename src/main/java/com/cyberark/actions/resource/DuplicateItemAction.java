package com.cyberark.actions.resource;

import com.cyberark.Consts;
import com.cyberark.PolicyBuilder;
import com.cyberark.Util;
import com.cyberark.actions.ActionType;
import com.cyberark.components.PolicyDisplayPane;
import com.cyberark.dialogs.InputDialog;
import com.cyberark.exceptions.ResourceAccessException;
import com.cyberark.models.*;
import com.cyberark.views.Icons;
import com.fasterxml.jackson.core.JsonProcessingException;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.cyberark.Consts.DARK_BG;
import static com.cyberark.components.PolicyDisplayPane.RESOURCE_ID;

@SelectionBasedAction
public class DuplicateItemAction<T extends ResourceModel> extends ActionBase<T> {
  private PolicyDisplayPane policyDisplayPane;
  public DuplicateItemAction(Supplier<T> selectedResource) {
    this(selectedResource, "Duplicate...");
  }

  public DuplicateItemAction(Supplier<T> selectedResource, String text) {
    super(text, ActionType.DuplicateItem, selectedResource);
    putValue(SHORT_DESCRIPTION, "Generate a policy of the selected resource");
    putValue(SMALL_ICON, Icons.getInstance().getIcon(Icons.ICON_CLONE,
        16,
        DARK_BG));
    putValue(MNEMONIC_KEY, KeyEvent.VK_D);
    putValue(Action.ACCELERATOR_KEY,
        KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.CTRL_DOWN_MASK));
    setEnabled(false);
  }

  @Override
  public void actionPerformed(ResourceModel resource) {
    ResourceIdentifier resourceIdentifier = resource.getIdentifier();
    String id = String.format("Copy-of-%s", resource.getIdentifier().getId());
    ResourceType type = resourceIdentifier.getType();

    try {
      final ArrayList<List<Membership>> membershipsList = new ArrayList<>(2);


      try {
        membershipsList.add(Util.isRoleResource(type)
            ? getMembership(resourceIdentifier)
            : new ArrayList<>());
        membershipsList.add(Util.isRoleResource(type)
            ? getMembers(resourceIdentifier)
            : new ArrayList<>());
      } catch (ResourceAccessException ex) {
        return; // called method in super class displays the error
      }

      final List<Membership> memberships = membershipsList.get(0);
      final List<Membership> members = membershipsList.get(1);

      policyDisplayPane = new PolicyDisplayPane(
          id,
          getResourcesService().getPolicies(),
          getPolicyText(resource, id, members, memberships));

      policyDisplayPane.setPropertyChangeListener(
          evt -> rebuildPolicy(
            type,
            evt.getPropertyName(),
            evt.getNewValue(),
            memberships,
            members
          )
      );

      showPolicyForm(policyDisplayPane);
    } catch (ResourceAccessException | JsonProcessingException ex) {
      showErrorDialog(ex);
    }
  }

  private void rebuildPolicy(ResourceType type, String propertyName, Object newResourceName,
                             List<Membership> memberships, List<Membership> members) {
    if (RESOURCE_ID.equals(propertyName)
        && Util.stringIsNotNullOrEmpty(newResourceName)) {
      policyDisplayPane.setPolicyText(
        getPolicyText(
          getSelectedResource(),
          newResourceName.toString(),
          members,
          memberships
        )
      );
    }
  }

  private String getPolicyText(ResourceModel resource,
                               String id,
                               List<Membership> members,
                               List<Membership> memberships) {
    PolicyBuilder pb = new PolicyBuilder();
    ResourceIdentifier resourceIdentifier = resource.getIdentifier();
    ResourceIdentifier copy = ResourceIdentifier.fromString(
        resourceIdentifier.getAccount(),
        resourceIdentifier.getType(),
        id
    );

    ResourceIdentifier owner = resource.getOwner() != null
        ? ResourceIdentifier.fromString(resource.getOwner()):
        null;

    if (owner != null && "admin".compareTo(owner.getId()) != 0) {
      pb.resource(copy, owner);
    } else {
      pb.resource(copy);
    }

    if (members.size() > 1) { // admin is always a member
      pb.grants(
          resourceIdentifier,
          members
              .stream()
              .map(i -> ResourceIdentifier.fromString(i.getMember()))
              .filter(i -> "admin".compareTo(i.getId()) != 0)
              .collect(Collectors.toList())
      );
    }

    if (memberships.size() > 0) {
      pb.grants(
          memberships
              .stream()
              .map(i -> ResourceIdentifier.fromString(i.getRole()))
              .collect(Collectors.toList()),
          copy
      );
    }

    return pb.toPolicy();
  }

  private void showPolicyForm(PolicyDisplayPane policyDisplayPane) throws ResourceAccessException,
      JsonProcessingException {
    InputDialog dlg = new InputDialog(getMainForm(),
        String.format("%s - Duplicate Resource", Consts.APP_NAME),
        true,
        policyDisplayPane, true);

    if (dlg.showDialog() == InputDialog.OK_OPTION) {
      String policyText = policyDisplayPane.getPolicyText();
      String policyBranch = policyDisplayPane.getBranch();

      if (policyText != null && policyText.trim().length() > 0) {
        String response = getResourcesService().loadPolicy(
            policyText,
            policyBranch == null
                ? "root"
                : policyBranch
        );

        if (policyDisplayPane.isCopyPermissions()) {
          getResourcesService().copyPermissions(
              getSelectedResource(),
              policyDisplayPane.getResourceId()
          );
        }

        if (getSelectedResource() instanceof RoleModel) {
          ResourceIdentifier identifier = getSelectedResource().getIdentifier();

          if (Util.stringIsNotNullOrEmpty(response)) {
            promptToCopyApiKeyToClipboard(response, ResourceIdentifier.deriveFrom(
                identifier, policyDisplayPane.getResourceId()));
          }
        }
      }

      fireEvent(getSelectedResource());
    }
  }
}
