package com.cyberark.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ResourceModel implements DataModel {
  public String created_at;
  public String id;
  public String owner;
  public String policy;
  public Permission[] permissions = new Permission[0];
  public Annotation[] annotations = new Annotation[0];

  public ResourceModel() {
  }

  @JsonCreator
  public ResourceModel(
      @JsonProperty("created_at") String created_at,
      @JsonProperty("id") String id,
      @JsonProperty("owner") String owner,
      @JsonProperty("policy") String policy,
      @JsonProperty("permissions") Permission[] permissions,
      @JsonProperty("annotations") Annotation[] annotations) {
    this.created_at = created_at;
    this.id = id;
    this.owner = owner;
    this.policy = policy;
    this.permissions = permissions;
    this.annotations = annotations;
  }

  public ResourceModel(String fullyQualifiedId) {
    this.id = fullyQualifiedId;
  }

  public ResourceIdentifier getIdentifier() {
    return ResourceIdentifier.fromString(id);
  }
}
