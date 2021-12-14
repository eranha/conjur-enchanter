package com.cyberark.controllers;

import com.cyberark.Application;
import com.cyberark.Credentials;
import com.cyberark.actions.LogoutAction;
import com.cyberark.components.MainForm;
import com.cyberark.dialogs.LoginDialog;
import com.cyberark.event.ApplicationEvent;
import com.cyberark.event.EventPublisher;
import com.cyberark.event.EventType;
import com.cyberark.event.LoginEventResult;
import com.cyberark.exceptions.LoginException;
import com.cyberark.resource.Endpoints;
import com.cyberark.resource.ResourceApiProvider;
import com.cyberark.resource.ResourceServiceFactory;
import com.cyberark.resource.RestApiResourceProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.net.URL;

class AuthnControllerImpl implements AuthnController {
  private final Application app = Application.getInstance();
  private InactivityListener inactivityListener;
  private static final Logger logger = LogManager.getLogger(AuthnControllerImpl.class);

  @Override
  public void login() {
    logger.trace("login::enter");

    ViewController viewController = ControllerFactory.getInstance().getViewController();
    MainForm form = createMainForm(viewController);
    Application.getInstance().setMainFrame(form);

    logger.debug("show login dialog");
    showLoginDialog(form);

    if (Application.getInstance().getCredentials() != null) {
      logger.debug("Publish logout event");
      EventPublisher.getInstance().fireEvent(new ApplicationEvent(EventType.Login));

      logger.debug("Start inactivity listener");
      inactivityListener.setInterval(7);
      inactivityListener.start();
    } else {
      logger.debug("User aborted, application is exiting");
      System.exit(1);
    }

    logger.trace("login::exit");
  }

  private MainForm createMainForm(ViewController viewController) {
    logger.debug("Constructing application main form");
    MainForm form = new MainForm(viewController::setView, viewController::actionPerformed);
    logger.debug("Constructing InactivityListener");
    inactivityListener = new InactivityListener(form, new LogoutAction());
    logger.debug("Application manin form created");
    return form;
  }


  private void showLoginDialog(JFrame parent) {
    logger.trace("showLoginDialog::enter");
    new LoginDialog(parent, e -> {
      final LoginDialog dlg = (LoginDialog) e.getSource();
      try {
        LoginEventResult evt = (LoginEventResult) e;
        char[] apiKey = doLogin(evt);
        JDialog d = (JDialog) e.getSource();

        logger.debug("Hide login dialog");
        d.setVisible(false);

        logger.debug("Dispose login dialog");
        d.dispose();

        logger.debug("Set application credentials: url={}, account={}, user={}",
            evt.url, evt.account, evt.user);
        app.setCredentials(new Credentials(evt.url, evt.account, evt.user, apiKey));
      } catch (Exception ex) {
        dlg.setErrorStatus(String.format("%s", ex.getMessage()));
        logger.error(ex);
      }
    });

    logger.trace("showLoginDialog::exit");
  }

  private char[] doLogin(LoginEventResult evt) throws LoginException {
    logger.trace("doLogin::enter");
    char[] apiKey;

    try {
      ResourceApiProvider http = new RestApiResourceProvider();
      logger.debug("Validate url: {}", evt.url);
      http.validateUrl(evt.url);
      logger.debug("url: {} is valid", evt.url);
      URL url = new URL(String.format(Endpoints.LOGIN, evt.url, evt.account));
      logger.debug("call ResourceApiProvider.get(url={}) and return response", url);
      apiKey = http.get(url, evt.user, evt.password).toCharArray();
    } catch (Exception e) {
      throw new LoginException(e);
    }

    logger.debug("ResourceApiProvider.get(url={}) and returned apiKey.length: {}", evt.url, apiKey.length);
    logger.trace("doLogin::exit");
    return apiKey;
  }

  @Override
  public void logout() {
    logger.trace("logout::enter");

    logger.debug("Reset application credentials");
    app.setCredentials(null);

    logger.debug("Stop inactivity listener");
    inactivityListener.stop();

    logger.debug("Reset resources service");
    ResourceServiceFactory.getInstance().setResourcesService(null);

    logger.debug("Publish logout event");
    EventPublisher.getInstance().fireEvent(new ApplicationEvent(EventType.Logout));

    logger.trace("logout::exit");
  }
}
