package com.cyberark.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;

public class Resources {
  private static final Properties strings = new Properties();
  private static final String DEFAULT_LANG_FILE = "/strings.en-US.properties";
  private static final Logger logger = LogManager.getLogger(Resources.class);

  static {
    Locale currentLocale = Locale.getDefault();
    String language = currentLocale.getLanguage();
    String country = currentLocale.getCountry();

    try {
      String localeStringsFile = String.format("/strings.%s-%s.properties", language, country);
      InputStream inputStream = Resources.class.getResourceAsStream(
          localeStringsFile
      );

      if (Objects.isNull(inputStream)) {
        logger.warn(
            "Locale strings file '{}' not found, fallback to default '{}'",
            localeStringsFile, DEFAULT_LANG_FILE
        );

        inputStream = Resources.class.getResourceAsStream(DEFAULT_LANG_FILE);
      }

      strings.load(inputStream);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static String getString(String key) {
    return strings.containsKey(key) ? strings.getProperty(key) : key;
  }
}
