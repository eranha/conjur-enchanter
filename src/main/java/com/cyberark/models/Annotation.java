package com.cyberark.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
public class Annotation {
  @Getter(AccessLevel.PUBLIC)
  @Setter(AccessLevel.PUBLIC)
  private String name;

  @Getter(AccessLevel.PUBLIC)
  @Setter(AccessLevel.PUBLIC)
  private String value;

  @Getter(AccessLevel.PUBLIC)
  @Setter(AccessLevel.PUBLIC)
  private String policy;

  @JsonCreator
  public Annotation(@JsonProperty("name") String name,
                    @JsonProperty("value") String value,
                    @JsonProperty("policy") String policy) {
    this.name = name;
    this.value = value;
    this.policy = policy;
  }
}
