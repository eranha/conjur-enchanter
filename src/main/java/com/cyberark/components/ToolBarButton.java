package com.cyberark.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ToolBarButton {

  private final static Dimension BUTTON_SIZE = new Dimension(96,32);
  public final static Color COLOR_BUTTON_MOUSEOVER = new Color(58, 78, 85);
  private final static Color COLOR_BUTTON_PRESSED = Color.darkGray;
  private final AbstractButton abstractButton;
  private boolean selected;

  public ToolBarButton(AbstractButton abstractButton) {
    this.abstractButton = abstractButton;
    initializeComponent();
  }

  public AbstractButton getButton() {
    return abstractButton;
  }

  public ToolBarButton(Action a) {
    abstractButton = new JButton(a);
    initializeComponent();
  }

  private void initializeComponent() {
    abstractButton.setBorder(BorderFactory.createEmptyBorder());
    abstractButton.setBackground(null);
    abstractButton.setOpaque(true);
    abstractButton.setForeground(Color.white);
    abstractButton.setPreferredSize(BUTTON_SIZE);
    abstractButton.setMaximumSize(BUTTON_SIZE);
    abstractButton.setMinimumSize(BUTTON_SIZE);

    abstractButton.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseReleased(MouseEvent e) {
        abstractButton.setBackground(abstractButton.isEnabled() ? COLOR_BUTTON_MOUSEOVER : null);
      }

      @Override
      public void mousePressed(MouseEvent e) {
        abstractButton.setBackground(abstractButton.isEnabled() ? COLOR_BUTTON_PRESSED : null);
      }

      @Override
      public void mouseEntered(MouseEvent e) {
        abstractButton.setBackground(abstractButton.isEnabled() ? COLOR_BUTTON_MOUSEOVER : null);
      }

      @Override
      public void mouseExited(MouseEvent e) {
        if (!selected) {
          abstractButton.setBorder(BorderFactory.createEmptyBorder());
          abstractButton.setBackground(null);
        }
      }
    });

    if (abstractButton instanceof JToggleButton) {
      abstractButton.addItemListener(e -> {
        selected = e.getStateChange() == ItemEvent.SELECTED;
        abstractButton.setBackground(e.getStateChange() == ItemEvent.SELECTED
            ? COLOR_BUTTON_PRESSED
            : null );
      });
    }
  }
}
