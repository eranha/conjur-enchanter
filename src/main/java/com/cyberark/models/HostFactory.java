package com.cyberark.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class HostFactory extends ResourceModel {

  private String[] layers = new String[0];
  private HostFactoryToken[] tokens = new HostFactoryToken[0];

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

  public HostFactory() {
  }

  public String[] getLayers() {
    return layers;
  }

  public HostFactoryToken[] getTokens() {
    return tokens;
  }

  public void setLayers(String[] layers) {
    this.layers = layers;
  }
}
