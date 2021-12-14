package com.cyberark.components;

import javax.swing.*;
import java.awt.*;

import static com.cyberark.Consts.*;

public class TitlePanel extends JPanel {
  private final JLabel titleLabel = new JLabel();
  private final JPanel titlePane = new JPanel();
  private final JPanel contentPane = new JPanel();
  private final Color titleBackground;

  public TitlePanel(String title, Component component, Color titleBackground) {
    this(title, titleBackground);
    setContent(component);
  }

  public TitlePanel(String title, Color titleBackground) {
    setTitle(title);
    this.titleBackground = titleBackground;
    initializeComponents();
  }

  private void initializeComponents() {
    setLayout(new BorderLayout());
    add(titlePane, BorderLayout.NORTH);
    add(getContentPane(), BorderLayout.CENTER);
    getContentPane().setLayout(new BorderLayout());
    titlePane.setLayout(new BorderLayout());
    titlePane.setBorder(BorderFactory.createEmptyBorder(0,4,0,0));
    titlePane.setBackground(titleBackground);
    titlePane.add(titleLabel, BorderLayout.CENTER);
    titlePane.setPreferredSize(new Dimension(32, 32));
    titleLabel.setFont(LABEL_FONT);
    titleLabel.setForeground(LABEL_FOREGROUND);
  }

  public JPanel getContentPane() {
    return contentPane;
  }

  public void setContent(Component comp) {
    getContentPane().add(comp, BorderLayout.CENTER);
  }

  public void setTitle(String text) {
    titleLabel.setText(text);
  }
}
