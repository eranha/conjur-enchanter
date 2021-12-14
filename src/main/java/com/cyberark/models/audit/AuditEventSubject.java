package com.cyberark.models.audit;

public class AuditEventSubject {
  public String role;
  public String resource;
  public String privilege;
  public String owner;
  public String member;
  public String annotation;

  @Override
  public String toString() {
    return "AuditEventSubject{" +
        "role='" + role + '\'' +
        ", resource='" + resource + '\'' +
        ", privilege='" + privilege + '\'' +
        ", owner='" + owner + '\'' +
        ", member='" + member + '\'' +
        ", annotation='" + annotation + '\'' +
        '}';
  }
}
