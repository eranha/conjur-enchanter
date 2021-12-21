package com.cyberark.components;

import javax.swing.*;
import java.awt.*;

import static com.cyberark.Consts.HTML_PARAGRAPH;

/**
 * An operation panel for general use, constructed as follows:
 *
 * title (bold text)
 * description
 * -----------
 * content
 *
 */
public class Form extends JPanel {
  public Form(String title, String description, Component content) {
    initializeComponents(title, description, content);
  }

  private void initializeComponents(String title, String description, Component content) {
    setLayout(new BorderLayout());

    // title (bold text)
    addTitle(title);

    JPanel panel = new JPanel(new BorderLayout());
    // description
    panel.add(getDescriptionLabel(description), BorderLayout.NORTH);

    // ----horizontal rule----
    // content
    panel.add(content, BorderLayout.CENTER);

    add(panel, BorderLayout.CENTER);
  }

  private JPanel getDescriptionLabel(String description) {
    JPanel label = new JPanel(new BorderLayout());
    JPanel hr = new JPanel(new BorderLayout());
    JPanel line = new JPanel();

    label.setBorder(BorderFactory.createEmptyBorder(0,0, 16,8));
    label.add(
        new JLabel(String.format(HTML_PARAGRAPH, 400, description), JLabel.LEFT),
        BorderLayout.NORTH
    );
    hr.setBorder(BorderFactory.createEmptyBorder(8,0, 0,0));
    line.setBorder(BorderFactory.createLineBorder(Color.lightGray, 1));
    line.setPreferredSize(new Dimension(1,2));
    hr.add(line, BorderLayout.CENTER);
    label.add(hr, BorderLayout.CENTER);
    return label;
  }

  private void addTitle(String title) {
    JLabel titleLabel = new JLabel(title);
    titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 14));
    titleLabel.setBorder(BorderFactory.createEmptyBorder(0,0,8,0));
    add(titleLabel, BorderLayout.NORTH);
  }
}
