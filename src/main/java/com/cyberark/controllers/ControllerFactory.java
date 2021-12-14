package com.cyberark.controllers;

public class ControllerFactory {
  private final ViewControllerImpl viewController  = new ViewControllerImpl();
  private final static ControllerFactory instance = new ControllerFactory();
  private final AuthnController authnController  = new AuthnControllerImpl();

  private ControllerFactory() {
  }

  public static ControllerFactory getInstance() {
    return instance;
  }

  public ViewController getViewController() {
    return viewController;
  }

  public AuthnController getAuthnController() {
    return authnController;
  }


}
