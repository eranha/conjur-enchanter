package com.cyberark.event;

import java.net.URL;
import java.util.HashMap;

public class ApiCallEvent extends ApplicationEvent {
  private final int responseCode;
  private final URL url;
  private final String requestMethod;
  private final HashMap<String, String> headers;
  private final String body;
  private final String response;

  public ApiCallEvent(URL url,
                      String requestMethod,
                      HashMap<String, String> headers,
                      String body,
                      int responseCode,
                      String response) {
    super(EventType.ApiCall);
    this.url = url;
    this.requestMethod = requestMethod;
    this.headers = headers;
    this.body = body;
    this.responseCode = responseCode;
    this.response = response;
  }

  public URL getUrl() {
    return url;
  }

  public String getRequestMethod() {
    return requestMethod;
  }

  public HashMap<String, String> getHeaders() {
    return headers;
  }

  public String getBody() {
    return body;
  }

  public String getResponse() {
    return response;
  }

  public int getResponseCode() {
    return responseCode;
  }
}
