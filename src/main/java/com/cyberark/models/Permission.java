package com.cyberark.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Permission {
  private String privilege;
  private String role;
  private String policy;

  public Permission() {
  }

  @JsonCreator
  public Permission(@JsonProperty("privilege") String privilege,
                    @JsonProperty("role") String role,
                    @JsonProperty("policy") String policy) {
    this.privilege = privilege;
    this.role = role;
    this.policy = policy;
  }

  @Override
  public String toString() {
    return "Permission{" +
        "privilege='" + privilege + '\'' +
        ", role='" + role + '\'' +
        ", policy='" + policy + '\'' +
        '}';
  }

  public String getPrivilege() {
    return privilege;
  }

  public void setPrivilege(String privilege) {
    this.privilege = privilege;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  public String getPolicy() {
    return policy;
  }

  public void setPolicy(String policy) {
    this.policy = policy;
  }
}
