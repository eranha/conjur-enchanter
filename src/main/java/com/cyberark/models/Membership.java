package com.cyberark.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Membership {
  private final String role;
  private final String member;
  private final String policy;

  @JsonCreator
  public Membership(
      @JsonProperty("admin_option") boolean admin_option,
      @JsonProperty("ownership") boolean ownership,
      @JsonProperty("role") String role,
      @JsonProperty("member") String member,
      @JsonProperty("policy") String policy) {
    this.role = role;
    this.member = member;
    this.policy = policy;
  }

  public String getRole() {
    return role;
  }

  public String getMember() {
    return member;
  }

  public String getPolicy() {
    return policy;
  }

  @Override
  public String toString() {
    return "Membership{" +
        "role='" + role + '\'' +
        ", member='" + member + '\'' +
        ", policy='" + policy + '\'' +
        '}';
  }
}
