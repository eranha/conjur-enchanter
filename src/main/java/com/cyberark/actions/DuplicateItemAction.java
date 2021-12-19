package com.cyberark.actions;

import com.cyberark.Consts;
import com.cyberark.PolicyTranslator;
import com.cyberark.Util;
import com.cyberark.components.PolicyDisplayPane;
import com.cyberark.dialogs.InputDialog;
import com.cyberark.exceptions.ResourceAccessException;
import com.cyberark.models.*;
import com.fasterxml.jackson.core.JsonProcessingException;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

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

      // TODO switch to policy builder
      StringBuilder policy = PolicyTranslator.toPolicy(resource, type, id, members, memberships);

      policyDisplayPane = new PolicyDisplayPane(
          id,
          getResourcesService().getPolicies(),
          policy.toString());

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
      // TODO switch to policy builder
      StringBuilder updatedPolicy = PolicyTranslator.toPolicy(
          getSelectedResource(),
          type,
          newResourceName.toString(),
          members,
          memberships);
      policyDisplayPane.setPolicyText(updatedPolicy.toString());
    }
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
