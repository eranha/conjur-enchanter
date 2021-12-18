package com.cyberark.views;

import com.cyberark.Errors;
import com.cyberark.exceptions.ApiCallException;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

public class ErrorView {
  public static void showApiCallErrorMessage(ApiCallException ex) {
    showApiCallErrorMessage(ex, new HashMap<>());
  }

  public static void showApiCallErrorMessage(ApiCallException ex, Map<Integer, String> errors) {
    ViewFactory viewFactory = ViewFactory.getInstance();
    int responseCode = ex.getResponseCode();
    String errorMessageKeyScope = responseCode > 403 ? "policy" : "error";
    String errorMessageKey = errors.get(responseCode);
    String errorMessage = errors.get(responseCode);

    if (responseCode > 0) {
      String errorPattern = Errors.getErrorMessage("error.pattern");
      JLabel message = new JLabel();

      if (errorMessageKey == null) {
        // fallback to general error
        if (Errors.getErrorMessage(String.format("%s.%s", errorMessageKeyScope, responseCode)) == null) {
          errorMessageKeyScope = "error";
        }
        errorMessage = Errors.getErrorMessage(String.format("%s.%s", errorMessageKeyScope, responseCode));
      } else {
        errorMessage = Errors.getErrorMessage(String.format("%s.%s", errorMessageKey, responseCode));
      }

      message.setText(
          String.format(
              errorPattern,
              Errors.getHttpStatusText(responseCode),
              errorMessage
          )
      );

      viewFactory.getMessageView().showMessageDialog(
          message,
          "Error",
          JOptionPane.ERROR_MESSAGE);
    }
  }

  public static void showErrorMessage(Exception ex) {
    if (ex.getCause() instanceof ApiCallException) {
      ApiCallException cause = (ApiCallException) ex.getCause();
      showApiCallErrorMessage(cause);
    } else {
      showErrorMessage(ex.getMessage());
    }
  }

  public static void showErrorMessage(String msg) {
    ViewFactory viewFactory = ViewFactory.getInstance();

    viewFactory.getMessageView().showMessageDialog(
        msg,
        "Error",
        JOptionPane.ERROR_MESSAGE);
  }
}
