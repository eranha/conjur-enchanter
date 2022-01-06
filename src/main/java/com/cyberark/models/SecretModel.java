package com.cyberark.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@ToString
@NoArgsConstructor
public class SecretModel extends ResourceModel {
  @Getter(AccessLevel.PUBLIC)
  @Setter(AccessLevel.PUBLIC)
  private Secret[] secrets;

  @JsonIgnore
  @Getter(AccessLevel.PUBLIC)
  @Setter(AccessLevel.PUBLIC)
  private char[] secret = new char[0];

  @JsonCreator
  public SecretModel(
      @JsonProperty("created_at") String createdAt,
      @JsonProperty("id") String id,
      @JsonProperty("owner") String owner,
      @JsonProperty("policy") String policy,
      @JsonProperty("permissions") Permission[] permissions,
      @JsonProperty("annotations") Annotation[] annotations,
      @JsonProperty("secrets") Secret[] secrets) {
   super(createdAt, id, owner, policy, permissions, annotations);
   this.secrets = secrets;
  }
}
