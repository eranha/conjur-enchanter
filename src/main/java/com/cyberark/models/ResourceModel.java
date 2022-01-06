package com.cyberark.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
public class ResourceModel implements DataModel {
  @Getter(AccessLevel.PUBLIC)
  private String createdAt;

  @Getter(AccessLevel.PUBLIC)
  @Setter(AccessLevel.PUBLIC)
  private String id;

  @Getter(AccessLevel.PUBLIC)
  @Setter(AccessLevel.PUBLIC)
  private String owner;

  @Getter(AccessLevel.PUBLIC)
  @Setter(AccessLevel.PUBLIC)
  private String policy;

  @Getter(AccessLevel.PUBLIC)
  private Permission[] permissions = new Permission[0];

  @Getter(AccessLevel.PUBLIC)
  @Setter(AccessLevel.PUBLIC)
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
}
