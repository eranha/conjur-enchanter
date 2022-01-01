package com.cyberark.models;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

public class HostFactoryTokensFormModel {
  private String[] restrictions;
  private final String hostFactoryId;
  private Instant expiration;
  private int numberOfTokens = 1;
  private int expirationDays;
  private int expirationHours;
  private int expirationMinutes;

  public HostFactoryTokensFormModel(String hostFactoryId) {
    this.hostFactoryId = hostFactoryId;
  }

  private void setExpiration() {
    Calendar cal = Calendar.getInstance();

    cal.add(Calendar.DATE, expirationDays);
    cal.add(Calendar.HOUR, expirationHours);
    cal.add(Calendar.MINUTE, expirationMinutes);

    this.expiration = cal.getTime().toInstant();
  }

  public int getNumberOfTokens() {
    return numberOfTokens;
  }

  public String[] getRestrictions() {
    return restrictions;
  }

  public String getExpirationUtcCDate() {
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    return dtf.format(LocalDateTime.ofInstant(getExpiration(), ZoneId.of("UTC") ));
  }

  private Instant getExpiration() {
    setExpiration();
    return expiration;
  }

  public void setRestrictions(String[] restrictions) {
    this.restrictions = restrictions;
  }

  public void setNumberOfTokens(int numberOfTokens) {
    if (numberOfTokens < 1) {
        throw new IllegalArgumentException(String.valueOf(numberOfTokens));
    }
    this.numberOfTokens = numberOfTokens;
  }

  public void setExpirationDays(int expirationDays) {
    this.expirationDays = expirationDays;
    setExpiration();
  }

  public void setExpirationHours(int expirationHours) {
    this.expirationHours = expirationHours;
    setExpiration();
  }

  public void setExpirationMinutes(int expirationMinutes) {
    this.expirationMinutes = expirationMinutes;
    setExpiration();
  }

  public String getHostFactoryId() {
    return hostFactoryId;
  }


  public static void main(String[] args) {

  }

  public long getExpirationDuration() {
    return Duration.ofDays(expirationDays).plusHours(expirationHours).plusMinutes(expirationMinutes).toMillis();
  }
}
