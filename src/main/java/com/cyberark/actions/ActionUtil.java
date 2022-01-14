package com.cyberark.actions;

import com.cyberark.Util;
import com.cyberark.models.ResourceIdentifier;
import com.cyberark.util.Resources;
import com.fasterxml.jackson.core.JsonProcessingException;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

import static com.cyberark.util.Resources.getString;

public class ActionUtil {
  public static void promptToCopyApiKeyToClipboard(
      Component parentComponent,
      String response,
      ResourceIdentifier model) {
    String apiKey;

    try {
      apiKey = Util.extractApiKey(
          response,
          model.getFullyQualifiedId()
      );
    } catch (JsonProcessingException e) {
      /* response is the api key not a json */
      apiKey = response;
    }

    if (apiKey == null) {
      JOptionPane
          .showMessageDialog(parentComponent,
              String.format(getString("rotate.api.key.message"), model.getId()),
              getString("rotate.api.key.message.dialog.title"),
              JOptionPane.INFORMATION_MESSAGE);
      return;
    }

    JLabel label = new JLabel(getString("copy.api.key.to.clipboard"));
    JLabel label2 = new JLabel(getString("only.once.label"));
    JTextField jt = new JTextField(apiKey);
    jt.addAncestorListener(new AncestorListener()
    {
      public void ancestorAdded ( AncestorEvent event )
      {
        jt.requestFocus();
        jt.selectAll();
      }
      public void ancestorRemoved ( AncestorEvent event ) {}
      public void ancestorMoved ( AncestorEvent event ) {}
    });

    Object[] choices = getStrings("copy.api.options");
    Object defaultChoice = choices[0];

    int answer = JOptionPane
        .showOptionDialog(parentComponent, new Component[]{label, label2,jt},
            getString("copy.api.key.to.clipboard.dialog.title"),
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE, null, choices, defaultChoice);

    if (answer == JOptionPane.YES_OPTION) {
      Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      clipboard.setContents(new StringSelection(apiKey), null);
    }
  }

  public static String[] getStrings(String key) {
    return Resources.getString(key).split(",");
  }

}

