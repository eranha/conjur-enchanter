package com.cyberark.exceptions;

import java.io.IOException;
import java.net.URL;

public class ApiCallException extends IOException {
  private final URL url;
  private int responseCode;

  public ApiCallException(String message, URL url, IOException cause, int responseCode) {
    super(message, cause);
    this.url = url;
    setResponseCode(responseCode);
  }

  @Override
  public String toString() {
    return "ApiCallException{" +
        "url=" + url +
        ", responseCode=" + responseCode +
        ", message=" + getMessage() +
        ", cause=" + getCause().toString() +
        '}';
  }

  public void setResponseCode(int responseCode) {
    this.responseCode = responseCode;
  }

  public int getResponseCode() {
    return responseCode;
  }
}
