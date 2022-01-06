package com.cyberark.models.hostfactory;

import com.cyberark.models.Annotation;
import com.cyberark.models.Permission;
import com.cyberark.models.ResourceModel;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public class HostFactory extends ResourceModel {
  @JsonIgnore
  @Getter(AccessLevel.PUBLIC)
  @Setter(AccessLevel.PUBLIC)
  private List<String> hosts = new ArrayList<>();

  @Getter(AccessLevel.PUBLIC)
  @Setter(AccessLevel.PUBLIC)
  private String[] layers = new String[0];

  @Getter(AccessLevel.PUBLIC)
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
}
