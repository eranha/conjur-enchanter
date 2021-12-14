package com.cyberark.event;

import java.awt.event.ActionEvent;

public class LoginEventResult extends ActionEvent {
  public String url;
  public String account;
  public String user;
  public char[] password;

  public LoginEventResult(Object source, String url, String account, String user, char[] password) {
    super(source,0, "login");
    this.url = url;
    this.account = account;
    this.user = user;
    this.password = password;
  }
}
