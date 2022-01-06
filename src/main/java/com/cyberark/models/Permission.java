package com.cyberark.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
public class Permission {
  @Getter(AccessLevel.PUBLIC)
  @Setter(AccessLevel.PUBLIC)
  private String privilege;

  @Getter(AccessLevel.PUBLIC)
  @Setter(AccessLevel.PUBLIC)
  private String role;

  @Getter(AccessLevel.PUBLIC)
  @Setter(AccessLevel.PUBLIC)
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
}
