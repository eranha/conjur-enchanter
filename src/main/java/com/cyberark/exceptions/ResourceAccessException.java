package com.cyberark.exceptions;

public class ResourceAccessException extends Exception {
  public ResourceAccessException(Exception e) {
    super(e);
  }
}
