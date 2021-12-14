package com.cyberark.models;

public class Privilege {
  private String privilege;
  private boolean allow;

  public Privilege(String privilege, Boolean allow) {
    this.privilege = privilege;
    this.allow = allow;
  }

  public String getPrivilege() {
    return privilege;
  }

  public boolean isAllow() {
    return allow;
  }

  public void setPrivilege(String privilege) {
    this.privilege = privilege;
  }

  public void setAllow(boolean allow) {
    this.allow = allow;
  }
}
