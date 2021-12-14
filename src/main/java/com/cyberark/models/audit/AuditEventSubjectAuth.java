package com.cyberark.models.audit;

public class AuditEventSubjectAuth {
  public String user;
  public String service;
  public String authenticator;

  @Override
  public String toString() {
    return "AuditEventAuth{" +
        "user='" + user + '\'' +
        ", service='" + service + '\'' +
        ", authenticator='" + authenticator + '\'' +
        '}';
  }
}
