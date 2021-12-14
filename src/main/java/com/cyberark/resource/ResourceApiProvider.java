package com.cyberark.resource;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

public interface ResourceApiProvider {
  String get(URL url, String user, char[] password) throws IOException;

  String get(URL url, char[] token) throws IOException;

  String post(URL url, char[] token, String body) throws IOException;

  String post(URL url, HashMap<String, String> headers, String body) throws IOException;

  String request(URL url, String requestMethod, char[] token, String body)
      throws IOException;

  void validateUrl(String address) throws IOException;
}
