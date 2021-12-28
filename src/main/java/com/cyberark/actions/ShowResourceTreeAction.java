package com.cyberark.actions;

import com.cyberark.Application;
import com.cyberark.PolicyBuilder;
import com.cyberark.Util;
import com.cyberark.components.ResourceTreeBrowser;
import com.cyberark.dialogs.InputDialog;
import com.cyberark.exceptions.ResourceAccessException;
import com.cyberark.models.*;
import com.cyberark.resource.ResourceServiceFactory;
import com.cyberark.resource.ResourcesService;
import com.cyberark.views.ErrorView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ShowResourceTreeAction extends AbstractAction {
  public ShowResourceTreeAction() {
    super("Resources");
    putValue(SHORT_DESCRIPTION, "View resources tree");
    putValue(MNEMONIC_KEY, KeyEvent.VK_T);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    Map<ResourceIdentifier, List<ResourceIdentifier>> policyToResources;
    HashMap<ResourceType, Map<ResourceIdentifier, ResourceModel>> resourceTypeToResources = new HashMap<>();

    try {
      Application.getInstance().getMainForm().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      ResourcesService resourcesService = ResourceServiceFactory
          .getInstance()
          .getResourcesService();
      policyToResources = resourcesService
          .getPolicyResources();

      ResourceIdentifier rootPolicy = policyToResources.keySet().stream()
          .filter(r -> r.getId().equals("root")).findFirst().orElseThrow();

      getResourceTypeResourceMap(resourceTypeToResources, resourcesService);

      PolicyBuilder policyBuilder = new PolicyBuilder();
      getPolicy(rootPolicy, policyBuilder, policyToResources, resourceTypeToResources);

    // TODO change the policy view to a policy level base view
    InputDialog.showDialog(
        Application.getInstance().getMainForm(),
         "Resources Browser",
        true,
        new ResourceTreeBrowser(
            new ArrayList<>(resourceTypeToResources.get(ResourceType.policy).values()),
            policyToResources, policyBuilder.toPolicy()),
        JOptionPane.OK_OPTION);
    } catch (ResourceAccessException ex) {
      ex.printStackTrace();
      ErrorView.showErrorMessage(ex);
    } finally {
      Application.getInstance().getMainForm().setCursor(Cursor.getDefaultCursor());
    }
  }

  private void getPolicy( ResourceIdentifier policy,
                          PolicyBuilder policyBuilder,
                          Map<ResourceIdentifier, List<ResourceIdentifier>> policyToResources,
                          Map<ResourceType, Map<ResourceIdentifier, ResourceModel>> resourceTypeToResources ) {
    policyBuilder.policy(policy);

    policyToResources.get(policy).forEach(
        r -> getResourcePolicy(
            policyBuilder, r, resourceTypeToResources
        ));

    policyToResources.keySet()
        .stream()
        .map(r -> resourceTypeToResources.get(ResourceType.policy).get(r))
        .filter(p -> p.getPolicy() != null && p.getPolicy().equals(policy.getFullyQualifiedId()))
        .forEach(p -> getPolicy(p.getIdentifier(), policyBuilder, policyToResources, resourceTypeToResources));
  }

  private void getResourceTypeResourceMap(HashMap<ResourceType, Map<ResourceIdentifier, ResourceModel>> resources,
                                          ResourcesService resourcesService) throws ResourceAccessException {
    for (ResourceType t : ResourceType.values()) {
      switch (t) {
        case policy:
          resources.put(t,
              resourcesService
                .getPolicies()
                .stream()
                .collect(Collectors.toMap(ResourceModel::getIdentifier, Function.identity()))
           );
          break;
        case host:
        case user:
          resources.put(t,
              resourcesService
                .getRoles(t)
                .stream()
                .collect(Collectors.toMap(ResourceModel::getIdentifier, Function.identity()))
              );
          break;
        case variable:
          resources.put(t,
              resourcesService
                  .getVariables()
                  .stream()
                  .collect(Collectors.toMap(ResourceModel::getIdentifier, Function.identity()))
          );
          break;
        default:
          resources.put(t,
              resourcesService
                  .getResources(t)
                  .stream()
                  .collect(Collectors.toMap(ResourceModel::getIdentifier, Function.identity()))
                  );
        break;
      }
    }
  }

  private void getResourcePolicy(PolicyBuilder policyBuilder,
                                 ResourceIdentifier resource,
                                 Map<ResourceType, Map<ResourceIdentifier, ResourceModel>> resources
                                 ) {
    ResourceModel model = resources.get(resource.getType()).get(resource);
    policyBuilder
          .resource(resource)
          .annotations(model.getAnnotations())
          .permissions(resource, model.getPermissions());

    List<Membership> members = new ArrayList<>();
    ResourceType type = resource.getType();
    ResourcesService resourcesService = ResourceServiceFactory
        .getInstance()
        .getResourcesService();

    if (Util.isSetResource(type)) { // applicable only for group/layer
      try {
        members = resourcesService.getMembers(resource).stream()
            .filter(r -> !(ResourceIdentifier.fromString(r.getMember())).getId().equals("admin"))
            .collect(Collectors.toList());
      } catch (ResourceAccessException ex) {
        ex.printStackTrace();
      }

      if (members.size() > 0) {
        // list members in a single grant
        policyBuilder.grants(resource, members.stream()
            .map(i -> ResourceIdentifier.fromString(i.getMember())).collect(Collectors.toList()));
      }
    }
  }
}
