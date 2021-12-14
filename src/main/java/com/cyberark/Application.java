package com.cyberark;

import com.cyberark.components.MainForm;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Application runtime context singelton class
 */
public class Application {
  private static final Application instance = new Application();
  private MainForm mainFrame;
  private final Properties settings = new Properties();

  private Application() {
    InputStream resourceAsStream = getClass().getResourceAsStream("/settings.properties");

    if (resourceAsStream == null) {
      throw new RuntimeException(new FileNotFoundException("/settings.properties"));
    }

    try {
      settings.load(resourceAsStream);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private Credentials credentials;

  public static Application getInstance() {
    return instance;
  }

  public Credentials getCredentials() {
    return credentials;
  }

  public String getAccount() {
    return credentials.account;
  }

  public void setCredentials(Credentials credentials) {
    this.credentials = credentials;
  }

  public MainForm getMainForm() {
    return mainFrame;
  }

  public void setMainFrame(MainForm mainFrame) {
    this.mainFrame = mainFrame;
  }

  public String getUser() {
    return credentials.user;
  }

  public String getSettingsProperty(String key) {
    return settings.getProperty(key);
  }
}
