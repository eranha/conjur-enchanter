package com.cyberark.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PolicyModel extends ResourceModel {
  private PolicyVersion[] policyVersions;

  @JsonCreator
  public PolicyModel(
      @JsonProperty("created_at") String created_at,
      @JsonProperty("id") String id,
      @JsonProperty("owner") String owner,
      @JsonProperty("policy") String policy,
      @JsonProperty("permissions") Permission[] permissions,
      @JsonProperty("annotations") Annotation[] annotations,
      @JsonProperty("policy_versions") PolicyVersion[] policyVersions) {
    super(created_at, id, owner, policy, permissions, annotations);
    this.policyVersions = policyVersions;
  }

  public PolicyVersion[] getPolicyVersions() {
    return policyVersions;
  }
}
