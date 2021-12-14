package com.cyberark.models;

public class Permission {
  public String privilege;
  public String role;
  public String policy;

  public Permission() {
  }

  public Permission(String privilege, String role, String policy) {
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
}
