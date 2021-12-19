package com.cyberark;

import com.cyberark.controllers.PolicyStatements;
import com.cyberark.models.*;

import java.util.*;
import java.util.stream.Collectors;

// TODO delete the class after resolving dependencies.
public class PolicyTranslator {

  public static StringBuilder toPolicy(ResourceModel model,
                                ResourceType type,
                                String id,
                                List<Membership> members,
                                List<Membership> memberships) {
    StringBuilder policy = new StringBuilder();

    policy.append(String.format(PolicyStatements.TYPE, type));
    policy.append(String.format(PolicyStatements.ID, id));

    if (model.getOwner() != null) {
      ResourceIdentifier owner = ResourceIdentifier.fromString(model.getOwner());
      policy.append(String.format(PolicyStatements.OWNER, owner.getType(), owner.getId()));
    }

    if (model.getAnnotations().length > 0) {
      policy.append(PolicyStatements.ANNOTATIONS).append(System.lineSeparator());

      for (Annotation annotation : model.getAnnotations()) {
        policy.append(String.format(PolicyStatements.NAME_VALUE, annotation.getName(), annotation.getValue()));
      }
    }

    if (model instanceof RoleModel) {
      String[] restricted_to = ((RoleModel) model).getRestrictedTo();
      if (restricted_to != null && restricted_to.length > 0) {
        policy.append(
            String.format(
                PolicyStatements.RESTRICTED_TO,
                Arrays.toString(restricted_to)));
      }
    }

    if (memberships != null) {
      policy.append(System.lineSeparator());
      policy.append(toGrants(
          memberships.stream().map(m -> ResourceIdentifier.fromString(m.getRole())).collect(Collectors.toList()),
          type,
          id));
    }

    return policy;
  }

  public static StringBuilder toGrants(List<ResourceIdentifier> roles,
                                       ResourceType memberType,
                                       String memberId) {
    StringBuilder policy = new StringBuilder();

    roles.forEach(role -> {
      policy.append(String.format(PolicyStatements.GRANT,
          role.getType(), role.getId()));
      policy.append(String.format(PolicyStatements.TYPE_ID, memberType, memberId));
      policy.append(System.lineSeparator());
    });

    return policy;
  }

  public static StringBuilder toPermissions(ResourceModel model) {
    return toPermissions(model, model.getIdentifier().getType(), model.getIdentifier().getId());
  }

  public static StringBuilder toPermissions(ResourceModel model, ResourceType type, String id) {
    StringBuilder policy = new StringBuilder();
    Map<String, List<String>> permissions = new HashMap<>();

    Arrays.stream(model.getPermissions()).forEach(p -> {
      permissions.computeIfAbsent(p.getRole(), v -> new ArrayList<>());
      permissions.get(p.getRole()).add(p.getPrivilege());
    });

    permissions.forEach((role, privileges) -> {
      ResourceIdentifier roleId = ResourceIdentifier.fromString(role);
      policy.append(String.format(PolicyStatements.PERMIT,
          type, id, String.join(",", privileges), roleId.getType(), roleId.getId()));
      policy.append(System.lineSeparator());
    });
    return policy;
  }
}
