package com.cyberark.components;

import com.cyberark.Util;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class RequiredTextField extends JTextField {
  public RequiredTextField(String text) {
    super(text);
    getDocument().addDocumentListener(new DefaultDocumentListener(e -> handleChangeEvent()));
  }

  private void handleChangeEvent() {
    Border border = (Util.isNullOrEmptyString(getText()))
        ? BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(1, 1, 1, 1),
            BorderFactory.createLineBorder(Color.pink, 1)
          )
        : UIManager.getBorder("TextField.border");

    setBorder(border);
  }
}
