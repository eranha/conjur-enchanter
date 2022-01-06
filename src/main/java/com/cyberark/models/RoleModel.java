package com.cyberark.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Represent a user or a host in the system.
 */
@ToString
public class RoleModel extends ResourceModel {
  @Getter(AccessLevel.PUBLIC)
  @Setter(AccessLevel.PUBLIC)
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
}
