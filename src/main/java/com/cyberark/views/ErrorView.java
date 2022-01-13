package com.cyberark.views;

import com.cyberark.Errors;
import com.cyberark.exceptions.ApiCallException;
import com.cyberark.util.Resources;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ErrorView {
  public static void showApiCallErrorMessage(ApiCallException ex) {
    showApiCallErrorMessage(ex, new HashMap<>());
  }

  public static void showApiCallErrorMessage(ApiCallException ex, Map<Integer, String> errors) {
    ViewFactory viewFactory = ViewFactory.getInstance();
    int responseCode = ex.getResponseCode();
    String errorMessageKeyScope = responseCode > 403 ? "policy" : "error";
    String errorMessageKey = errors.get(responseCode);
    String errorMessage;

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
          Resources.getString("error.view.title"),
          JOptionPane.ERROR_MESSAGE);
    }
  }

  public static void showErrorMessage(Exception ex) {
    ApiCallException cause = getApiCallException(ex);

    if (Objects.nonNull(cause)) {
      ex.printStackTrace();
      showApiCallErrorMessage(cause);
    } else {
      ex.printStackTrace();
      showErrorMessage(ex.toString());
    }
  }

  private static ApiCallException getApiCallException(Exception ex) {
    Throwable t = ex.getCause();

    while (t != null) {
      if (t instanceof ApiCallException) {
        return (ApiCallException) t;
      }

      t = t.getCause();
    }

    return null;
  }

  public static void showErrorMessage(String msg) {
    ViewFactory viewFactory = ViewFactory.getInstance();
    JTextArea jt = new JTextArea(msg, 8, 36);

    viewFactory.getMessageView().showMessageDialog(
        new JScrollPane(jt),
        "Error",
        JOptionPane.ERROR_MESSAGE);
  }
}
