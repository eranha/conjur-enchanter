package com.cyberark.controllers;

import com.cyberark.Credentials;
import com.cyberark.Util;
import com.cyberark.exceptions.AuthenticationException;
import com.cyberark.resource.Endpoints;
import com.cyberark.resource.ResourceApiProvider;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.HashMap;
import java.util.function.Supplier;

public class AccessTokenProviderImpl implements AccessTokenProvider {
  private static final int DEFAULT_LIFESPAN_SECONDS = 8 * 60;
  private final ResourceApiProvider resourceApiProvider;
  private String endpoint;
  private char[] apiKey;
  private final Supplier<Credentials> credentialsSupplier;
  private char[] token;
  private Instant tokenExpiration = Instant.MIN;

  public AccessTokenProviderImpl(ResourceApiProvider resourceProvider,
                                 Supplier<Credentials> credentialsSupplier) {
    this.resourceApiProvider = resourceProvider;
    this.credentialsSupplier = credentialsSupplier;
  }

  @Override
  public char[] getAccessToken() throws AuthenticationException {
    if (endpoint == null) {
      setEndpoint(credentialsSupplier.get());
    }

    if (token != null && tokenExpiration.isAfter(Instant.now())) {
      return token;
    }

    try {
      token = resourceApiProvider.post(
          new URL(endpoint),
          new HashMap<>(),
          new String(apiKey)).toCharArray();
      tokenExpiration = getTokenExpiration(token);
      return token;
    } catch (Exception e) {
      throw new AuthenticationException(e);
    }
  }

  private Instant getTokenExpiration(char[] token) throws JsonProcessingException {
    String claims = new String(Base64
        .getDecoder()
        .decode(Util.getNode(new String(token), "payload").textValue()));
    String iat = Util.getNode(claims, "iat").asText();

    return LocalDateTime.ofEpochSecond(Long.parseLong(iat), 0, ZoneOffset.UTC)
        .plusSeconds(DEFAULT_LIFESPAN_SECONDS)
        .toInstant(ZoneOffset.UTC);
  }

  private void setEndpoint(Credentials credentials) {
    endpoint = String.format(
        Endpoints.AUTHN,
        credentials.url,
        credentials.account,
        credentials.user);
    apiKey = credentials.loginApiKey;
  }
}
