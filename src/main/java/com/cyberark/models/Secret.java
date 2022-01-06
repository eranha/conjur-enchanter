package com.cyberark.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;

@ToString
public class Secret {
  @Getter(AccessLevel.PUBLIC)
  private final String version;

  private final String expiresAt;

  @JsonCreator
  public Secret(@JsonProperty("version") String version,
                @JsonProperty("expires_at") String expiresAt) {
    this.version = version;
    this.expiresAt = expiresAt;
  }
}
