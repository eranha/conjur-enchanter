package com.cyberark.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PolicyVersion {
  private final int version;
  private final String createdAt;
  private final String policyText;
  private final String policySha256;
  private final String finishedAt;
  private final String clientIp;
  private String id;
  private String role;

  @JsonCreator
  public PolicyVersion(@JsonProperty("version") int version,
                       @JsonProperty("created_at") String createdAt,
                       @JsonProperty("policy_text") String policyText,
                       @JsonProperty("policy_sha256") String policySha256,
                       @JsonProperty("finished_at") String finishedAt,
                       @JsonProperty("client_ip") String clientIp,
                       @JsonProperty("id") String id,
                       @JsonProperty("role") String role) {
    this.version = version;
    this.createdAt = createdAt;
    this.policyText = policyText;
    this.policySha256 = policySha256;
    this.finishedAt = finishedAt;
    this.clientIp = clientIp;
    this.id = id;
    this.role = role;
  }

  public int getVersion() {
    return version;
  }

  public String getCreatedAt() {
    return createdAt;
  }

  public String getPolicyText() {
    return policyText;
  }

  public String getPolicySha256() {
    return policySha256;
  }

  public String getFinishedAt() {
    return finishedAt;
  }

  public String getClientIp() {
    return clientIp;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }
}
