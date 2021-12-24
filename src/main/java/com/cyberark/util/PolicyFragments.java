package com.cyberark.util;

import com.cyberark.models.ResourceType;

public class PolicyFragments {
  public static String permitFragment() {
    String fragment = "- !permit%s" +
        "  role: !<kind-of-role> <role-name>%s" +
        "  privileges: [x, y, z]%s" +
        "  role: !<kind-of-role> <role-name>";

    return String.format(
        fragment,
        System.lineSeparator(),
        System.lineSeparator(),
        System.lineSeparator()
    );
  }

  public static String denyFragment() {
    String fragment = "- !deny%s" +
        "  role: !<kind-of-role> <role-name>%s" +
        "  privileges: [x, y, z]%s" +
        "  role: !<kind-of-role> <role-name>";

    return String.format(
        fragment,
        System.lineSeparator(),
        System.lineSeparator(),
        System.lineSeparator()
    );
  }

  public static String revokeFragment() {
    String fragment = "- !revoke%s" +
        "  role: !<kind-of-role> <role-name>%s" +
        "  member: !<kind-of-role> <role-name>";

    return String.format(
        fragment,
        System.lineSeparator(),
        System.lineSeparator()
    );
  }

  public static String grantFragment() {
    String fragment = "- !grant%s" +
        "  role: !<kind-of-role> <role-name>%s" +
        "  members:%s" +
        "    - !<kind-of-role> <role-name>%s" +
        "    - !<kind-of-role> <role-name>";

    return String.format(
        fragment,
        System.lineSeparator(),
        System.lineSeparator(),
        System.lineSeparator(),
        System.lineSeparator()
    );
  }

  public static String webserviceFragment(int elementCount) {
    String fragment = "- !webservice%n" +
        "  id: webservice%s%n" +
        "  owner: !<kind-of-role> <role-name>%n" +
        "  annotations:%n" +
        "    <key>: <value>";

    return String.format(
        fragment,
        elementCount
    );
  }

  public static String groupFragment(int elementCount) {
    String fragment = "- !group%n" +
        "  id: group%s%n" +
        "  owner: !<kind-of-role> <role-name>%n" +
        "  annotations:%n" +
        "    editable: true | false";

    return String.format(
        fragment,
        elementCount
    );
  }

  public static String variableFragment(int elementCount) {
    String fragment = "- !variable%n" +
        "  id: variable%s%n" +
        "  kind: <description>%n" +
        "  mime_type:%n" +
        "  annotations:%n" +
        "    <key>: <value>";

    return String.format(
        fragment,
        elementCount
    );
  }

  public static String layerFragment(int elementCount) {
    String fragment = "- !layer%n" +
        "  id: layer%s%n" +
        "  owner: !<kind-of-role> <role-name>%n" +
        "  annotations:%n" +
        "    <key>: <value>%n";

    return String.format(
        fragment,
        elementCount
    );
  }

  public static String actorRoleFragment(ResourceType type, int elementCount) {
    String fragment = "- !%s%n" +
        "  id: %s%s%n" +
        "  owner: !<kind-of-role> <role-name>%n" +
        "  annotations:%n" +
        "    <key>: <value>%n" +
        "  restricted_to: <network range>";

    return String.format(
        fragment,
        type,
        type,
        elementCount
    );
  }

  public static String policyFragment(int elementCount) {
    String fragment = "- !policy%n" +
        "  id: policy%s%n" +
        "  owner: !<kind-of-role> <role-name>%n" +
        "  body:%n" +
        "    <statements>%n";

    return String.format(
        fragment,
        elementCount
    );
  }

  public static String hostFactoryFragment(int elementCount) {
    String fragment = "- !host-factory%n" +
        "  id: host-factory%s%n" +
        "  owner: !<kind-of-role> <role-name>%n" +
        "  layers: [ !layer <layer-name>, !layer <layer-name> ]%n" +
        "  annotations:%n" +
        "    <key>: <value>";

    return String.format(
        fragment,
        elementCount
    );
  }
}
