package com.cyberark.controllers;

import com.cyberark.Credentials;
import com.cyberark.resource.Endpoints;
import com.cyberark.resource.ResourceApiProvider;
import com.cyberark.exceptions.AuthenticationException;

import java.net.URL;
import java.util.HashMap;
import java.util.function.Supplier;

public class AccessTokenProviderImpl implements AccessTokenProvider {
  private final ResourceApiProvider resourceApiProvider;
  private String endpoint;
  private char[] apiKey;
  private final Supplier<Credentials> credentialsSupplier;

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

    try {
      String accessToken = resourceApiProvider.post(
          new URL(endpoint),
          new HashMap<>(),
          new String(apiKey));
      return accessToken.toCharArray();
    } catch (Exception e) {
      throw new AuthenticationException(e);
    }
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
