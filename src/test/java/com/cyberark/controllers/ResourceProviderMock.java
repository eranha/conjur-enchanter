package com.cyberark.controllers;

import com.cyberark.resource.ResourceApiProvider;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

public class ResourceProviderMock implements ResourceApiProvider {

  private String returnValue;

  public void setReturnValue(String returnValue) {
    this.returnValue = returnValue;
  }

  @Override
  public String get(URL url, String user, char[] password) throws IOException {
    return returnValue;
  }

  @Override
  public String get(URL url, char[] token) throws IOException {
    return returnValue;
  }

  @Override
  public String post(URL url, char[] token, String body) throws IOException {
    return returnValue;
  }

  @Override
  public String post(URL url, HashMap<String, String> headers, String body) throws IOException {
    return returnValue;
  }

  @Override
  public String request(URL url, String requestMethod, char[] token, String body) throws IOException {
    return returnValue;
  }

  @Override
  public void validateUrl(String address) throws IOException {

  }
}
