package com.cyberark.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SecretModel extends ResourceModel {
  private Secret[] secrets;

  @JsonIgnore
  private char[] secret;

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

  public SecretModel() {
  }

  public Secret[] getSecrets() {
    return secrets;
  }

  public char[] getSecret() {
    return secret;
  }

  public void setSecret(char[] secret) {
    this.secret = secret;
  }
}
