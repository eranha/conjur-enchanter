package com.cyberark.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represent a user or a host in the system.
 */
public class RoleModel extends ResourceModel {
  public String[] restricted_to;


  public RoleModel() {
  }

  @JsonCreator
  public RoleModel(@JsonProperty("created_at") String created_at,
                   @JsonProperty("id") String id,
                   @JsonProperty("owner") String owner,
                   @JsonProperty("policy") String policy,
                   @JsonProperty("permissions") Permission[] permissions,
                   @JsonProperty("annotations") Annotation[] annotations,
                   @JsonProperty("restricted_to") String[] restricted_to) {
    super(created_at, id, owner, policy, permissions, annotations);
    this.restricted_to = restricted_to;
  }
}
