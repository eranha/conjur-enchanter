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

import static com.cyberark.util.Resources.getString;

public class ShowResourceTreeAction extends AbstractAction {
  public ShowResourceTreeAction() {
    super(getString("show.resource.tree.action.text"));
    putValue(SHORT_DESCRIPTION, getString("show.resource.tree.action.description"));
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

      getResourceTypeResourceMap(resourceTypeToResources, resourcesService);

      Map<ResourceIdentifier, String> policyToTextMap = getPolicyToTextMap(
          policyToResources,
          resourceTypeToResources
      );

    InputDialog.showDialog(
        Application.getInstance().getMainForm(),
         getString("show.resource.tree.action.dialog.title"),
        true,
        new ResourceTreeBrowser(
            new ArrayList<>(resourceTypeToResources.get(ResourceType.policy).values()),
            policyToResources,
            policyToTextMap),
        JOptionPane.OK_OPTION);
    } catch (ResourceAccessException ex) {
      ex.printStackTrace();
      ErrorView.showErrorMessage(ex);
    } finally {
      Application.getInstance().getMainForm().setCursor(Cursor.getDefaultCursor());
    }
  }

  private Map<ResourceIdentifier, String> getPolicyToTextMap(
                                  Map<ResourceIdentifier, List<ResourceIdentifier>> policyToResources,
                                  Map<ResourceType, Map<ResourceIdentifier, ResourceModel>> resourceTypeToResources ) {

    Map<ResourceIdentifier, String> policyToText = new HashMap<>();

    policyToResources
        .forEach((policy, resources) ->
            {
              StringBuilder policyText = new StringBuilder();
              resources.forEach(r -> policyText.append(getResourcePolicy(r, resourceTypeToResources)));
              policyToText.put(policy, policyText.toString());
            }
        );

    return policyToText;
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
        case host_factory:
          resources.put(t,
              resourcesService
                  .getHostFactories()
                  .stream()
                  .collect(Collectors.toMap(ResourceModel::getIdentifier, Function.identity()))
          );
          break;
        case host:
          resources.put(t,
              resourcesService
                  .getRoles(t)
                  .stream()
                  .collect(Collectors.toMap(ResourceModel::getIdentifier, Function.identity()))
          );
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

  private String getResourcePolicy(ResourceIdentifier resource,
                                 Map<ResourceType, Map<ResourceIdentifier, ResourceModel>> resources) {
    PolicyBuilder policyBuilder = new PolicyBuilder();
    ResourceModel model = resources.get(resource.getType()).get(resource);
    String id = resource.getId();
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
    return policyBuilder.toPolicy();
  }
}
