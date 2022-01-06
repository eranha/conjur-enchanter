package com.cyberark.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
public class Membership {
  @Getter(AccessLevel.PUBLIC)
  private final String role;

  @Getter(AccessLevel.PUBLIC)
  private final String member;

  @Getter(AccessLevel.PUBLIC)
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
}
