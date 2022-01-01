package com.cyberark.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class HostFactory extends ResourceModel {

  private final String[] layers;
  private final HostFactoryToken[] tokens;

  @JsonCreator
public HostFactory(@JsonProperty("created_at") String createdAt,
                     @JsonProperty("id") String id,
                     @JsonProperty("owner") String owner,
                     @JsonProperty("policy") String policy,
                     @JsonProperty("permissions") Permission[] permissions,
                     @JsonProperty("annotations") Annotation[] annotations,
                     @JsonProperty("layers") String[] layers,
                     @JsonProperty("tokens") HostFactoryToken[] tokens) {
    super(createdAt, id, owner, policy, permissions, annotations);
    this.layers = layers;
    this.tokens = tokens;
  }

  public String[] getLayers() {
    return layers;
  }

  public HostFactoryToken[] getTokens() {
    return tokens;
  }
}
