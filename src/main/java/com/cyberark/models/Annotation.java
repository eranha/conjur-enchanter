package com.cyberark.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Annotation {
  public String name;
  public String value;
  public String policy;

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
}
