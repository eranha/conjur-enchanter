package com.cyberark.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Secret {
  private final String version;
  private final String expiresAt;

  @JsonCreator
  public Secret(@JsonProperty("version") String version,
                @JsonProperty("expires_at") String expiresAt) {
    this.version = version;
    this.expiresAt = expiresAt;
  }

  public String getVersion() {
    return version;
  }
}
