package com.cyberark.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class SecretModel extends ResourceModel {
  public Secret[] secrets;

  @JsonIgnore
  public char[] secret;

  public SecretModel() {
  }
}
