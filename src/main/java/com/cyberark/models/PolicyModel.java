package com.cyberark.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PolicyModel extends ResourceModel {
  public PolicyVersion[] policy_versions;

  @JsonCreator
  public PolicyModel(
      @JsonProperty("created_at") String created_at,
      @JsonProperty("id") String id,
      @JsonProperty("owner") String owner,
      @JsonProperty("policy") String policy,
      @JsonProperty("permissions") Permission[] permissions,
      @JsonProperty("annotations") Annotation[] annotations,
      @JsonProperty("policy_versions") PolicyVersion[] policy_versions) {
    super(created_at, id, owner, policy, permissions, annotations);
    this.policy_versions = policy_versions;
  }
}
