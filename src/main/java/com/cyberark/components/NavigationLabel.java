package com.cyberark.components;

import com.cyberark.views.Icons;
import com.cyberark.views.ViewType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

import static com.cyberark.Consts.*;

public class NavigationLabel extends JPanel {
  private final ViewType view;
  private boolean selected;
  private final JPanel leftPanel = new JPanel();
  private final JPanel mainPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));

  public NavigationLabel(ViewType view, Consumer<NavigationLabel> consumer) {
    this.view = view;
    initializeComponents(view);
    addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        super.mousePressed(e);
        consumer.accept(NavigationLabel.this);
      }
    });
  }

  public void setSelectedState(boolean selected) {
    this.selected = selected;
    highlight(selected);
  }

  private void initializeComponents(ViewType view) {
    setBackground(DARK_BG);
    setLayout(new BorderLayout());
    setPreferredSize(new Dimension(260, 86));
    setMaximumSize(new Dimension(260, 86));

    leftPanel.setPreferredSize(new Dimension(4, 32));

    add(leftPanel, BorderLayout.WEST);
    leftPanel.setBackground(DARK_BG);

    JLabel icon  = new JLabel(Icons.getInstance().getIcon(view));
    icon.setPreferredSize(new Dimension(32, 32));
    mainPanel.add(icon);
    mainPanel.setBackground(DARK_BG);

    JLabel text = new JLabel(view.toString());
    text.setFont(LABEL_FONT);
    text.setForeground(Color.WHITE);
    mainPanel.add(text);
    mainPanel.setBorder(BorderFactory.createEmptyBorder(16,8,8,0));

    add(mainPanel, BorderLayout.CENTER);

    addMouseListener(new MouseAdapter() {
      @Override
      public void mouseEntered(MouseEvent e) {
        super.mouseEntered(e);
        highlight(true);
      }

      @Override
      public void mouseExited(MouseEvent e) {
        super.mouseExited(e);
        if (!selected) {
          highlight(false);
        }
      }
    });
  }

  private void highlight(boolean flag) {
    mainPanel.setBackground(flag ? Color.BLACK : DARK_BG);
    leftPanel.setBackground(flag ? LIGHT_COLOR : DARK_BG);
  }

  public ViewType getViewType() {
    return view;
  }
}
