package com.cyberark.models.audit;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuditEventSubjectData {
  @JsonProperty("auth@43868")
  public AuditEventSubjectAuth auth;

  @JsonProperty("action@43868")
  public AuditEventSubjectAction action;

  @JsonProperty("client@43868")
  public AuditEventSubjectClient client;

  @JsonProperty("subject@43868")
  public AuditEventSubject subject;

  @JsonProperty("policy@43868")
  public AuditEventSubjectPolicy policy;

  @Override
  public String toString() {
    return "AuditEventSubjectData{" +
        "auth=" + auth +
        ", action=" + action +
        ", client=" + client +
        ", subject=" + subject +
        ", policy=" + policy +
        '}';
  }
}
