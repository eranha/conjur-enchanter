package com.cyberark;

import org.apache.commons.httpclient.HttpStatus;

import java.io.IOException;
import java.util.Properties;

public class Errors {
  private static Properties errors;

  public static String getErrorMessage(String key) {
    if (errors == null) {
      loadProperties();
    }

    return errors.getProperty(key);
  }

  public static String getHttpStatusText(int responseCode) {
    return String.format(
        "%s - %s",
        responseCode,
        HttpStatus.getStatusText(responseCode)
    );
  }

  private static void loadProperties() {
    errors = new Properties();
    try {
      errors.load(Errors.class.getResourceAsStream("/errors.properties"));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
