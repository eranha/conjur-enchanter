package com.cyberark.controllers;

public interface AuthnController {
  /**
   * Execute login process, displays a credentials dialog.
   */
  void login();

  /**
   * Logs out the user and clears all data.
   */
  void logout();
}
