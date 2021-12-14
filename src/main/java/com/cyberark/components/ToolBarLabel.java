package com.cyberark.components;

import javax.swing.*;
import java.awt.*;

public class ToolBarLabel extends JPanel {

  private JLabel label;
  public ToolBarLabel(String text, Icon icon) {
    initializeComponent(text, icon);
  }

  public void setText(String text) {
    label.setText(text);
  }

  private void initializeComponent(String text, Icon icon) {
    setLayout(new BorderLayout());
    setOpaque(false);
    label = new JLabel(text, icon, SwingConstants.RIGHT);
    label.setForeground(Color.WHITE);
    add(label, BorderLayout.CENTER);
  }
}
