package com.cyberark.models.hostfactory;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

@lombok.RequiredArgsConstructor()
public class HostFactoryTokensFormModel {
  @Getter(AccessLevel.PUBLIC)
  @Setter(AccessLevel.PUBLIC)
  private String[] restrictions;

  @NonNull
  @Getter(AccessLevel.PUBLIC)
  private final String hostFactoryId;

  private Instant expiration;

  @Getter(AccessLevel.PUBLIC)
  private int numberOfTokens = 1;

  private int expirationDays;
  private int expirationHours;
  private int expirationMinutes;

  private void setExpiration() {
    Calendar cal = Calendar.getInstance();

    cal.add(Calendar.DATE, expirationDays);
    cal.add(Calendar.HOUR, expirationHours);
    cal.add(Calendar.MINUTE, expirationMinutes);

    this.expiration = cal.getTime().toInstant();
  }

  public String getExpirationUtcCDate() {
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    return dtf.format(LocalDateTime.ofInstant(getExpiration(), ZoneId.of("UTC") ));
  }

  private Instant getExpiration() {
    setExpiration();
    return expiration;
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

  public long getExpirationDuration() {
    return Duration.ofDays(expirationDays).plusHours(expirationHours).plusMinutes(expirationMinutes).toMillis();
  }
}
