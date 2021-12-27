package com.cyberark;

import com.cyberark.models.Annotation;
import com.cyberark.models.Permission;
import com.cyberark.models.ResourceIdentifier;
import com.cyberark.models.ResourceType;

import java.util.*;

/**
 * This class bares the knowledge to convert a resource class and resource related
 * objects into a policy.
 * The pattern use in this class is builder pattern.
 */
public class PolicyBuilder {
  private final StringBuilder policy = new StringBuilder();
  private int indent = 0 ;

  public PolicyBuilder() {
  }

  public PolicyBuilder policy(String id, ResourceIdentifier owner) {
    append("- !policy");
    append("  id: %s", id);
    append("  owner: !%s %s", owner.getType(), owner.getId());
    append("  body:");
    indent += 4;
    return this;
  }

  public PolicyBuilder revoke(ResourceIdentifier member, List<ResourceIdentifier> roles) {
      roles.forEach(r -> revoke(member, r));
      return this;
  }

  public PolicyBuilder revoke(ResourceIdentifier member, ResourceIdentifier role) {
    append("- !revoke");
    append("  role: !%s %s", role.getType(), role.getId());
    append("  member: !%s %s", member.getType(), member.getId());
    return this;
  }

  public PolicyBuilder resource(ResourceType type) {
    append("- !%s", type);
    return this;
  }

  public PolicyBuilder policy(String id) {
    append("- !policy");
    append("  id: %s", id);
    append("  body:");
    indent += 4;
    return this;
  }

  public PolicyBuilder resource(ResourceIdentifier resource) {
    append("- !%s", resource.getType());
    append("  id: %s",  resource.getId());
    return this;
  }

  public PolicyBuilder restrictions(String[] restrictions) {
    if (restrictions != null && restrictions.length > 0) {
      append(
          "  restricted_to: %s",
          Arrays.toString(restrictions)
      );
    }

    return this;
  }

  public PolicyBuilder resource(ResourceIdentifier resource,
                                ResourceType owner) {
    append("- !%s", resource.getType());
    append("  id: %s", resource.getId());
    append("  owner: !%s", owner);
    return this;
  }

  public PolicyBuilder resource(ResourceIdentifier resource, ResourceIdentifier owner) {
    append("- !%s", resource.getType());
    append("  id: %s", resource.getId());
    append("  owner: !%s %s", owner.getType(), owner.getId());
    return this;
  }

  public PolicyBuilder delete(ResourceIdentifier resource) {
    append("- !delete");
    append("  record: !%s %s", resource.getType(), resource.getId());
    return this;
  }

  public PolicyBuilder deny(ResourceIdentifier role,
                              ResourceIdentifier resource,
                              Set<String> privileges) {
    append("- !deny");
    append("  resource: !%s %s", resource.getType(), resource.getId());
    append("  privileges: [%s]", String.join(",", privileges));
    append("  role: !%s %s", role.getType(), role.getId());
    return this;
  }

  public PolicyBuilder annotations(Annotation[] annotations) {
    if (annotations.length > 0) {
      append("  annotations:");
      Arrays.stream(annotations).forEach(annotation -> append(
             "    %s: \"%s\"",
          annotation.getName(), annotation.getValue()));
    }
    return this;
  }

  /**
   * The !grant statement adds roles to another role for the purpose of inheriting all
   * of the privileges given to the grant role.
   * @param role This is the granting role. kind-of-role must be either group or layer.
   * @param members List of members are roles to add into the grant.
   *
   * @return this PolicyBuilder
   */
  public PolicyBuilder grants(ResourceIdentifier role, List<ResourceIdentifier> members ) {
    append("- !grant");
    append("  role: !%s %s", role.getType(), role.getId());
    append("  members:");

    members.forEach(
        member ->
        append("    - !%s %s", member.getType(), member.getId())
    );

    return this;
  }

  public PolicyBuilder grants(List<ResourceIdentifier> roles, ResourceIdentifier member ) {
    roles.forEach(r -> grant(r, member));
    return this;
  }

  /**
   * The !grant statement adds roles to another role for the purpose of inheriting all
   * of the privileges given to the grant role.
   * @param role This is the granting role. kind-of-role must be either group or layer.
   * @param member Member is the role to add into the grant.
   *
   * @return this PolicyBuilder
   */
  public PolicyBuilder grant(ResourceIdentifier role, ResourceIdentifier member ) {
    append("- !grant");
    append("  role: !%s %s", role.getType(), role.getId());
    append("  members:");
    append("    - !%s %s", member.getType(), member.getId());

    return this;
  }

  /**
   * The !permit statement provides the authorization for roles to access resources.
   * Adds the resource argument permission to this policy builder.
   * @param permissions Identifies the resource that is gaining access to the resource.
   * @return this PolicyBuilder
   */
  public PolicyBuilder permissions(ResourceIdentifier resource, Permission[] permissions) {
    Map<ResourceIdentifier, Set<String>> map = new HashMap<>();

    Arrays.stream(permissions).forEach(p -> {
      ResourceIdentifier role = ResourceIdentifier.fromString(p.getRole());
      map.computeIfAbsent(role , v -> new HashSet<>());
      map.get(role).add(p.getPrivilege());
    });

    map.forEach((role, privileges) -> permit(role, resource, privileges));

    return this;
  }
  /**
   * The !permit statement provides the authorization for roles to access resources.
   * @param role Identifies the role that is gaining access to the resource.
   * @param resource Identifies the resource whose access is being controlled.
   *                 The resource type can be policy, user, host, group, layer, or variable.
   * @param privileges Assigns the type of access the role has on the resource.
   * @return this policy builder
   */
  public PolicyBuilder permit(ResourceIdentifier role,
                              ResourceIdentifier resource,
                              Set<String> privileges) {
    append("- !permit");
    append("  resource: !%s %s", resource.getType(), resource.getId());
    append("  privileges: [%s]", String.join(",", privileges));
    append("  role: !%s %s", role.getType(), role.getId());
    return this;
  }


  public PolicyBuilder revoke(List<ResourceIdentifier> members, ResourceIdentifier role) {
    members.forEach(i -> revoke(i, role));
    return this;
  }

  public String toPolicy() {
    return policy.toString();
  }



  private void append(String format, Object... args) {
    append(String.format(format, args));
  }

  private void append(String line) {
    policy.append(padLeftSpaces(line)).append(System.lineSeparator());
  }

  private String padLeftSpaces(String inputString) {
    return (indent == 0)
        ? inputString
        : " ".repeat(Math.max(0, indent)) + inputString;
  }

  public PolicyBuilder policy(ResourceIdentifier policy) {
    return policy(policy.getId());
  }
}
