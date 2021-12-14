package com.cyberark;

/**
 * This class holds login credentials to Conjur
 */
public class Credentials {
  public String url;
  public String account;
  public String user;
  public char[] loginApiKey;

  public Credentials(String url, String account, String user, char[] apiKey) {
    this.url = url;
    this.account = account;
    this.user = user;
    this.loginApiKey = apiKey;
  }
}
