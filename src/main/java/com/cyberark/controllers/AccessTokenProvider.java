package com.cyberark.controllers;

import com.cyberark.exceptions.AuthenticationException;

public interface AccessTokenProvider {
  char[] getAccessToken() throws AuthenticationException;
}
