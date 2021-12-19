package com.cyberark.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Annotation {
  private String name;
  private String value;
  private String policy;

  @JsonCreator
  public Annotation(@JsonProperty("name") String name,
                    @JsonProperty("value") String value,
                    @JsonProperty("policy") String policy) {
    this.name = name;
    this.value = value;
    this.policy = policy;
  }

  @Override
  public String toString() {
    return "Annotation{" +
        "name='" + name + '\'' +
        ", value='" + value + '\'' +
        ", policy='" + policy + '\'' +
        '}';
  }

  public String getName() {
    return name;
  }

  public String getValue() {
    return value;
  }

  public String getPolicy() {
    return policy;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public void setPolicy(String policy) {
    this.policy = policy;
  }
}
