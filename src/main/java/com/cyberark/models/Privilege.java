package com.cyberark.models;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public class Privilege {
  @Getter(AccessLevel.PUBLIC)
  @Setter(AccessLevel.PUBLIC)
  private String privilege;

  @Getter(AccessLevel.PUBLIC)
  @Setter(AccessLevel.PUBLIC)
  private boolean isAllow;

  public Privilege(String privilege, Boolean isAllow) {
    this.privilege = privilege;
    this.isAllow = isAllow;
  }
}
