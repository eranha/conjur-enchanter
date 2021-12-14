package com.cyberark.exceptions;

public class AuthenticationException extends Exception {
  public AuthenticationException(Exception cause) {
    super(cause);
  }
}
