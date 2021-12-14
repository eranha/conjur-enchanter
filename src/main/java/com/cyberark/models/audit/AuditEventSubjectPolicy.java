package com.cyberark.models.audit;

public class AuditEventSubjectPolicy {
  public String id;
  public String version;

  @Override
  public String toString() {
    return "AuditEventSubjectPolicy{" +
        "id='" + id + '\'' +
        ", version='" + version + '\'' +
        '}';
  }
}
