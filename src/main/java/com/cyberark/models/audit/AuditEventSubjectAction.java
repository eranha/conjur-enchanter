package com.cyberark.models.audit;

public class AuditEventSubjectAction {
  public String operation;
  public String result;

  @Override
  public String toString() {
    return "AuditEventSubjectAction{" +
        "operation='" + operation + '\'' +
        ", result='" + result + '\'' +
        '}';
  }
}
