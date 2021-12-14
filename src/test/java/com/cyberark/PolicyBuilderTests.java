package com.cyberark;

import com.cyberark.models.Annotation;
import com.cyberark.models.Permission;
import com.cyberark.models.ResourceIdentifier;
import com.cyberark.models.ResourceType;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class PolicyBuilderTests {
  @Test
  public void policyTest() {
    StringBuilder expected = new StringBuilder();
    expected
        .append("- !policy").append(System.lineSeparator())
        .append("  id: my-policy").append(System.lineSeparator())
        .append("  body:").append(System.lineSeparator());
    PolicyBuilder p = new PolicyBuilder();
    assertEquals(expected.toString(), p.policy("my-policy").toPolicy());
  }

  @Test
  void policyWithOwner() {
    StringBuilder expected = new StringBuilder();
    expected
        .append("- !policy").append(System.lineSeparator())
        .append("  id: my-policy").append(System.lineSeparator())
        .append("  owner: !user foo").append(System.lineSeparator())
        .append("  body:").append(System.lineSeparator());
    PolicyBuilder p = new PolicyBuilder();
    assertEquals(expected.toString(), p.policy("my-policy",
        ResourceIdentifier.fromString("test:user:foo")).toPolicy());
  }

  @Test
  void policyWithBody() {
    StringBuilder expected = new StringBuilder();
    expected
        .append("- !policy").append(System.lineSeparator())
        .append("  id: my-policy").append(System.lineSeparator())
        .append("  owner: !user foo").append(System.lineSeparator())
        .append("  body:").append(System.lineSeparator())
        .append("    - !layer my-vms").append(System.lineSeparator())
        .append("    - !host").append(System.lineSeparator())
        .append("      id: my-host-1").append(System.lineSeparator())
        .append("      owner: !layer my-vms").append(System.lineSeparator())
        .append("    - !host").append(System.lineSeparator())
        .append("      id: my-host-2").append(System.lineSeparator())
        .append("      owner: !layer my-vms").append(System.lineSeparator());

    PolicyBuilder policyBuilder = new PolicyBuilder();
    policyBuilder
        .policy("my-policy", ResourceIdentifier.fromString("test:user:foo"))
        .resource(ResourceIdentifier.fromString("test:layer:my-vms"))
        .resource(
            ResourceIdentifier.fromString("test:host:my-host-1"),
            ResourceIdentifier.fromString("test:layer:my-vms"))
        .resource(
            ResourceIdentifier.fromString("test:host:my-host-2"),
            ResourceIdentifier.fromString("test:layer:my-vms"));

    assertEquals(expected.toString(), policyBuilder.toPolicy());
  }

  @Test
  void policyWithBodyOwnerInheritsIdFromPolicy() {
    StringBuilder expected = new StringBuilder();
    expected
        .append("- !policy").append(System.lineSeparator())
        .append("  id: my-policy").append(System.lineSeparator())
        .append("  body:").append(System.lineSeparator())
        .append("    - !layer").append(System.lineSeparator())
        .append("    - !host").append(System.lineSeparator())
        .append("      id: my-host-3").append(System.lineSeparator())
        .append("      owner: !layer").append(System.lineSeparator())
        .append("    - !host").append(System.lineSeparator())
        .append("      id: my-host-4").append(System.lineSeparator())
        .append("      owner: !layer").append(System.lineSeparator());

    PolicyBuilder policyBuilder = new PolicyBuilder();
    policyBuilder
        .policy("my-policy")
        .resource(ResourceType.layer)
        .resource(
            ResourceIdentifier.fromString("test:host:my-host-3"),
            ResourceType.layer)
        .resource(
            ResourceIdentifier.fromString("test:host:my-host-4"),
            ResourceType.layer);
    assertEquals(expected.toString(), policyBuilder.toPolicy());
  }

  @Test
  void resource() {
    StringBuilder expected = new StringBuilder("- !user foo");
    PolicyBuilder p = new PolicyBuilder();
    String policy = p.resource(ResourceIdentifier.fromString("test:user:foo")).toPolicy();
    assertEquals(expected.append(System.lineSeparator()).toString(), policy);
  }

  @Test
  void testResourceWithOwner() {
    StringBuilder expected = new StringBuilder();
    expected
        .append("- !host").append(System.lineSeparator())
        .append("  id: bar").append(System.lineSeparator())
        .append("  owner: !user foo").append(System.lineSeparator());

    PolicyBuilder p = new PolicyBuilder();
    String policy = p.resource(
        ResourceIdentifier.fromString("test:host:bar"),
        ResourceIdentifier.fromString("test:user:foo")
    ).toPolicy();
    assertEquals(expected.toString(), policy);
  }

  @Test
  void delete() {
    StringBuilder expected = new StringBuilder();
    expected
        .append("- !delete").append(System.lineSeparator())
        .append("  record: !user foo").append(System.lineSeparator());

    PolicyBuilder p = new PolicyBuilder();
    String policy = p.delete(
        ResourceIdentifier.fromString("test:user:foo")
    ).toPolicy();
    assertEquals(expected.toString(), policy);
  }

  @Test
  void revoke() {
    ResourceIdentifier role = ResourceIdentifier.fromString("test:group:admins");
    ResourceIdentifier role2 = ResourceIdentifier.fromString("test:group:su");
    ResourceIdentifier member = ResourceIdentifier.fromString("test:user:foo");
    StringBuilder expected = new StringBuilder();

    expected.append("- !revoke").append(System.lineSeparator())
        .append("  role: !group admins").append(System.lineSeparator())
        .append("  member: !user foo").append(System.lineSeparator())
        .append("- !revoke").append(System.lineSeparator())
        .append("  role: !group su").append(System.lineSeparator())
        .append("  member: !user foo").append(System.lineSeparator());

    PolicyBuilder p = new PolicyBuilder();
    p.revoke(member, role)
     .revoke(member, role2);

    assertEquals(expected.toString(), p.toPolicy());
  }

  @Test
  void revokeList() {
    ResourceIdentifier role = ResourceIdentifier.fromString("test:group:admins");
    ResourceIdentifier role2 = ResourceIdentifier.fromString("test:group:su");
    ResourceIdentifier member = ResourceIdentifier.fromString("test:user:foo");
    StringBuilder expected = new StringBuilder();

    expected.append("- !revoke").append(System.lineSeparator())
        .append("  role: !group admins").append(System.lineSeparator())
        .append("  member: !user foo").append(System.lineSeparator())
        .append("- !revoke").append(System.lineSeparator())
        .append("  role: !group su").append(System.lineSeparator())
        .append("  member: !user foo").append(System.lineSeparator());

    PolicyBuilder p = new PolicyBuilder();
    p.revoke(member, Arrays.stream((new ResourceIdentifier[]{role, role2})).collect(Collectors.toList()));

    assertEquals(expected.toString(), p.toPolicy());
  }

  @Test
  void revokeMembers() {
    ResourceIdentifier role = ResourceIdentifier.fromString("test:group:admins");
    ResourceIdentifier member = ResourceIdentifier.fromString("test:group:su");
    ResourceIdentifier member2 = ResourceIdentifier.fromString("test:user:foo");
    StringBuilder expected = new StringBuilder();

    expected.append("- !revoke").append(System.lineSeparator())
        .append("  role: !group admins").append(System.lineSeparator())
        .append("  member: !group su").append(System.lineSeparator())
        .append("- !revoke").append(System.lineSeparator())
        .append("  role: !group admins").append(System.lineSeparator())
        .append("  member: !user foo").append(System.lineSeparator());

    PolicyBuilder p = new PolicyBuilder();
    p.revoke(Arrays.stream((new ResourceIdentifier[]{member, member2})).collect(Collectors.toList()), role);

    assertEquals(expected.toString(), p.toPolicy());
  }

  @Test
  void deny() {
    ResourceIdentifier role = ResourceIdentifier.fromString("test:user:foo");
    ResourceIdentifier resource = ResourceIdentifier.fromString("test:variable:the_secret");
    Set<String> privileges = Arrays.stream((new String[]{"read,write"})).collect(Collectors.toSet());

    StringBuilder expected = new StringBuilder();

    expected
        .append("- !deny").append(System.lineSeparator())
        .append("  resource: !variable the_secret").append(System.lineSeparator())
        .append("  privileges: [read,write]").append(System.lineSeparator())
        .append("  role: !user foo").append(System.lineSeparator());

    PolicyBuilder p = new PolicyBuilder();
    p.deny(role, resource, privileges);
    assertEquals(expected.toString(), p.toPolicy());

  }

  @Test
  void annotations() {
    StringBuilder expected = new StringBuilder("- !user foo");
    expected.append(System.lineSeparator())
      .append("  annotations:").append(System.lineSeparator())
      .append("    a1: \"v1\"").append(System.lineSeparator())
      .append("    a2: \"v2\"").append(System.lineSeparator());

    Annotation[] annotations = new Annotation[] {
        new Annotation("a1", "v1", "test:policy:root"),
        new Annotation("a2", "v2", "test:policy:root")
    };

    PolicyBuilder builder = new PolicyBuilder();
    String policy = builder
        .resource(ResourceIdentifier.fromString("test:user:foo"))
        .annotations(annotations).toPolicy();
    assertEquals(expected.toString(), policy);
  }

  @Test
  void grants() {
    StringBuilder expected = new StringBuilder("- !grant");
    expected.append(System.lineSeparator())
        .append("  role: !user foo").append(System.lineSeparator())
        .append("  members:").append(System.lineSeparator())
        .append("    - !host bar").append(System.lineSeparator())
        .append("    - !user john").append(System.lineSeparator());

    PolicyBuilder p = new PolicyBuilder();
    p.grants(
        ResourceIdentifier.fromString("test:user:foo"),
        Arrays.stream(new ResourceIdentifier[] {
            ResourceIdentifier.fromString("test:host:bar"),
            ResourceIdentifier.fromString("test:user:john")
        }).collect(Collectors.toList())
        );
    assertEquals(expected.toString(), p.toPolicy());
  }

  @Test
  void testGrants() {
    StringBuilder expected = new StringBuilder();
    expected.append("- !grant")
        .append(System.lineSeparator())
        .append("  role: !host bar").append(System.lineSeparator())
        .append("  members:").append(System.lineSeparator())
        .append("    - !user foo").append(System.lineSeparator())
        .append("- !grant").append(System.lineSeparator())
        .append("  role: !user john").append(System.lineSeparator())
        .append("  members:").append(System.lineSeparator())
        .append("    - !user foo").append(System.lineSeparator());
    PolicyBuilder p = new PolicyBuilder();
    p.grants(
    Arrays.stream(new ResourceIdentifier[] {
        ResourceIdentifier.fromString("test:host:bar"),
        ResourceIdentifier.fromString("test:user:john")
    }).collect(Collectors.toList()), ResourceIdentifier.fromString("test:user:foo"));
    assertEquals(expected.toString(), p.toPolicy());
  }

  @Test
  void grant() {
    StringBuilder expected = new StringBuilder("- !grant");
    expected.append(System.lineSeparator())
        .append("  role: !user foo").append(System.lineSeparator())
        .append("  members:").append(System.lineSeparator())
        .append("    - !host bar").append(System.lineSeparator());

    PolicyBuilder p = new PolicyBuilder();
    p.grants(
        ResourceIdentifier.fromString("test:user:foo"),
        Arrays.stream(new ResourceIdentifier[] {
            ResourceIdentifier.fromString("test:host:bar"),
        }).collect(Collectors.toList())
    );
    assertEquals(expected.toString(), p.toPolicy());
  }

  @Test
  void permissions() {
    StringBuilder expected = new StringBuilder();
    expected.append("- !permit").append(System.lineSeparator())
            .append("  resource: !webservice ws1").append(System.lineSeparator())
            .append("  privileges: [execute]").append(System.lineSeparator())
            .append("  role: !user foo").append(System.lineSeparator());

    PolicyBuilder p = new PolicyBuilder();

    Permission[] permissions = new Permission[] {
        new Permission("execute", "test:user:foo", "test:policy:root")
    };
    p.permissions(ResourceIdentifier.fromString("test:webservice:ws1"), permissions);
    assertEquals(expected.toString(), p.toPolicy());
  }

  @Test
  void permit() {
    StringBuilder expected = new StringBuilder();
    expected.append("- !permit").append(System.lineSeparator())
        .append("  resource: !webservice ws1").append(System.lineSeparator())
        .append("  privileges: [read, execute]").append(System.lineSeparator())
        .append("  role: !user foo").append(System.lineSeparator())
        .append("- !permit").append(System.lineSeparator())
        .append("  resource: !webservice ws1").append(System.lineSeparator())
        .append("  privileges: [update]").append(System.lineSeparator())
        .append("  role: !host bar").append(System.lineSeparator());

    PolicyBuilder p = new PolicyBuilder();

    Permission[] permissions = new Permission[2];
    permissions[0] = new Permission("execute", "test:user:foo", "test:policy:root");
    permissions[1] = new Permission("update", "test:host:bar", "test:policy:root");

    p.permit(
        ResourceIdentifier.fromString("test:user:foo"),
        ResourceIdentifier.fromString("test:webservice:ws1"),
        Arrays.stream((new String[]{"read, execute"})).collect(Collectors.toSet()));

    p.permit(
        ResourceIdentifier.fromString("test:host:bar"),
        ResourceIdentifier.fromString("test:webservice:ws1"),
        Arrays.stream((new String[]{"update"})).collect(Collectors.toSet()));

    assertEquals(expected.toString(), p.toPolicy());
  }

  @Test
  void toPolicy() {
    PolicyBuilder p = new PolicyBuilder();
    String policy = p.resource(ResourceIdentifier.fromString("test:user:foo")).toPolicy();
    assertNotNull(policy);
    assertFalse(policy.length() == 0);
  }
}