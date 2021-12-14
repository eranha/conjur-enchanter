package com.cyberark.exceptions;

public class LoginException extends Exception {
  public LoginException(Exception cause) {
    this(cause.getMessage(), cause);
  }

  public LoginException(String message, Exception cause) {
    super(message, cause);
  }
}
