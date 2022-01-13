package com.cyberark.util;

import java.io.IOException;
import java.util.Properties;

public class Resources {
  private static Properties strings = new Properties();

  static {
    try {
      strings.load(Resources.class.getResourceAsStream("/strings.properties"));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static String getString(String key) {
    return strings.containsKey(key) ? strings.getProperty(key) : key;
  }
}
