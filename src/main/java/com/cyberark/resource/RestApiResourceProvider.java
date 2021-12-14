package com.cyberark.resource;

import com.cyberark.event.ApiCallEvent;
import com.cyberark.event.EventPublisher;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;

public class RestApiResourceProvider implements ResourceApiProvider {

  private static final Logger logger = LogManager.getLogger(RestApiResourceProvider.class);

  @Override
  public String get(URL url, String user, char[] password) throws IOException {
    return readResponse(openConnection(url, "GET", getAuthorizationHeader(user, password)));
  }

  @Override
  public String get(URL url, char[] token) throws IOException {
    logger.trace("enter get(url={})", url);
    HashMap<String, String> headers = new HashMap<>();
    headers.put("Authorization", formatAuthorizationHeader(
        token));
    HttpURLConnection connection = openConnection(url, "GET", headers);
    String response = readResponse(connection);
    fireApiCallEvent(url, "GET", headers, null, connection.getResponseCode(), response);
    logger.trace("get(url={}) exit return response length: {}", url, response.length());
    return response;
  }


  @Override
  public String post(URL url, char[] token, String body) throws IOException {
    return post(url, getAuthorizationHeader(token), body);
  }

  @Override
  public String post(URL url, HashMap<String, String> headers, String body) throws IOException {
    return request(url, "POST", headers, body);
  }

  @Override
  public String request(URL url, String requestMethod, char[] token, String body)
      throws IOException {

    return request(url, requestMethod, getAuthorizationHeader(token), body);
  }


  @Override
  public void validateUrl(String address) throws IOException {
    URL url;
    int responseCode;

    try {
      url = new URL(address);
    } catch (MalformedURLException e) {
      throw new IOException(String.format("Malformed URL: %s", e.getMessage()));
    }

    try {
      HttpURLConnection huc = (HttpURLConnection) url.openConnection();
      responseCode = huc.getResponseCode();
    } catch (IOException e) {
      throw new IOException(String.format("Connection error: %s", e.getMessage()));
    }

    if (HttpURLConnection.HTTP_OK != responseCode) {
      logger.error("HTTP response: {}", responseCode);
      throw new IOException(String.format("HTTP response code: %s", responseCode));
    }
  }

  private String request(URL url, String requestMethod, HashMap<String, String> headers, String body)
      throws IOException {

    HttpURLConnection conn = openConnection(url, requestMethod, headers, body);
    String response = readResponse(conn);
    fireApiCallEvent(url, requestMethod, headers, body, conn.getResponseCode(), response);

    return response;
  }


  private HttpURLConnection openConnection(URL url,
                                          String requestMethod,
                                          HashMap<String, String> headers) throws IOException {
    return openConnection(url, requestMethod, headers, null);
  }

  private HttpURLConnection openConnection(URL url,
                                          String requestMethod,
                                          HashMap<String, String> headers,
                                          String body) throws IOException {
    logger.trace("openConnection({}, {}) enter", url, requestMethod);

    HttpURLConnection conn = (HttpURLConnection) url.openConnection();

    if (headers != null) {
      headers.forEach(conn::setRequestProperty);
    }

    if ("PATCH".equals(requestMethod)) {
      conn.setRequestProperty("X-HTTP-Method-Override", "PATCH");
      conn.setRequestMethod("POST");
    } else {
      conn.setRequestMethod(requestMethod);
    }

    if (body != null) {
      conn.setDoOutput(true);
      try (OutputStream os = conn.getOutputStream()) {
        os.write(body.getBytes());
      }
    }

    logger.debug("Response code: {}", conn.getResponseCode());
    logger.trace("openConnection({}, {}) exit", url, requestMethod);
    return conn;
  }

  private void fireApiCallEvent(URL url, String requestMethod, HashMap<String, String> headers,
                                String body,
                                int responseCode,
                                String response) {
    EventPublisher.getInstance().fireEvent(new ApiCallEvent(url,
        requestMethod,
        headers,
        body,
        responseCode,
        response));
  }

  private String formatAuthorizationHeader(String user, char[] password) {
    String pattern = "Basic %s";
    return String.format(pattern,
        Base64.getEncoder().encodeToString(
            String.format("%s:%s", user, new String(password)).getBytes()));
  }

  private String formatAuthorizationHeader(char[] token) {
    String pattern = "Token token=\"%s\"";
    return String.format(pattern,
        Base64.getEncoder().encodeToString(new String(token).getBytes()));
  }

  private String readResponse(HttpURLConnection conn) throws IOException {
    logger.trace("readResponse({}) enter",conn);

    StringBuilder content;

    try (BufferedReader in = new BufferedReader(
        new InputStreamReader(conn.getInputStream()))) {
      String inputLine;
      content = new StringBuilder();
      while ((inputLine = in.readLine()) != null) {
        content.append(inputLine);
      }
    }

    logger.trace("readResponse({}) exit:: return content length: {}", conn, content.length());
    return content.toString();
  }


  private HashMap<String, String> getAuthorizationHeader(char[] token) {
    HashMap<String, String> headers = new HashMap<>();
    headers.put("Authorization", formatAuthorizationHeader(
        token));
    return headers;
  }

  private HashMap<String, String> getAuthorizationHeader(String user, char[] password) {
    HashMap<String, String> headers = new HashMap<>();
    headers.put("Authorization", formatAuthorizationHeader(
        user, password));
    return headers;
  }

}
