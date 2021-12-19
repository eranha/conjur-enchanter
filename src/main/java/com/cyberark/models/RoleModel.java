package com.cyberark.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represent a user or a host in the system.
 */
public class RoleModel extends ResourceModel {
  private String[] restrictedTo;


  public RoleModel() {
  }

  @JsonCreator
  public RoleModel(@JsonProperty("created_at") String createdAt,
                   @JsonProperty("id") String id,
                   @JsonProperty("owner") String owner,
                   @JsonProperty("policy") String policy,
                   @JsonProperty("permissions") Permission[] permissions,
                   @JsonProperty("annotations") Annotation[] annotations,
                   @JsonProperty("restricted_to") String[] restrictedTo) {
    super(createdAt, id, owner, policy, permissions, annotations);
    this.restrictedTo = restrictedTo;
  }

  public String[] getRestrictedTo() {
    return restrictedTo;
  }

  public void setRestrictedTo(String[] restrictedTo) {
    this.restrictedTo = restrictedTo;
  }
}
