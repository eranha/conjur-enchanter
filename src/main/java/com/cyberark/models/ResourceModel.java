package com.cyberark.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ResourceModel implements DataModel {
  private String createdAt;
  private String id;
  private String owner;
  private String policy;
  private Permission[] permissions = new Permission[0];
  private Annotation[] annotations = new Annotation[0];

  public ResourceModel() {
  }

  @JsonCreator
  public ResourceModel(
      @JsonProperty("created_at") String createdAt,
      @JsonProperty("id") String id,
      @JsonProperty("owner") String owner,
      @JsonProperty("policy") String policy,
      @JsonProperty("permissions") Permission[] permissions,
      @JsonProperty("annotations") Annotation[] annotations) {
    this.createdAt = createdAt;
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

  public String getCreatedAt() {
    return createdAt;
  }

  public String getId() {
    return id;
  }

  public String getOwner() {
    return owner;
  }

  public String getPolicy() {
    return policy;
  }

  public Permission[] getPermissions() {
    return permissions;
  }

  public Annotation[] getAnnotations() {
    return annotations;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

  public void setPolicy(String policy) {
    this.policy = policy;
  }

  public void setAnnotations(Annotation[] annotations) {
    this.annotations = annotations;
  }
}
