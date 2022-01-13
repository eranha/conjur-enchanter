package com.cyberark.components;

import com.cyberark.Util;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;

public class ApiKeysView extends ContainerBase {
  private final String message;

  public ApiKeysView(String message) {
    this.message = message;
    initializeComponents();
  }

  private void initializeComponents() {
    setLayout(new BorderLayout());
    JLabel label = new JLabel(getString("api.key.view.label1"));
    JLabel label2 = new JLabel(getString("api.key.view.label2"));

    JPanel topPanel = new JPanel(new GridLayout(3, 1));
    topPanel.add(label);
    topPanel.add(label2);
    topPanel.add(Box.createVerticalStrut(8));

    add(topPanel, BorderLayout.NORTH);

    String indented = Util.prettyPrintJson(message);
    final JTextArea jt = new JTextArea(indented);
    add(new JScrollPane(jt), BorderLayout.CENTER);

    final Highlighter.HighlightPainter painter =
        new DefaultHighlighter.DefaultHighlightPainter(Color.pink);
    final ArrayList<Point> points = getApiKeysPositions(indented);

    points.forEach(
        p -> {
          try {
            jt.getHighlighter().addHighlight(p.x, p.y, painter);
          } catch (BadLocationException e) {
            e.printStackTrace();
          }

          jt.setCaretPosition(p.x);
          jt.moveCaretPosition(p.x);
        }
    );

    jt.addFocusListener(new FocusListener() {
      @Override
      public void focusGained(FocusEvent e) {
        jt.getHighlighter().removeAllHighlights();

        if (points.size() > 0) {
          jt.select(points.get(0).x, points.get(0).y);
        }
      }

      @Override
      public void focusLost(FocusEvent e) {

      }
    });
  }

  private ArrayList<Point> getApiKeysPositions(String indented) {
    ArrayList<Point> points = new ArrayList<>();

    int index = 0;

    while (index < indented.length()) {
      index = indented.indexOf("\"api_key\"", index);
      if( index < 0) break;
      points.add(new Point(index + 13, indented.indexOf("\"", index + 13)));
      index = indented.indexOf("\"", index + 13);
    }

    return points;
  }
}
