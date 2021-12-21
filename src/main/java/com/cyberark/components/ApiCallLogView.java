package com.cyberark.components;

import com.cyberark.views.Icons;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import static com.cyberark.Consts.DARK_BG;

public class ApiCallLogView extends JPanel {
  private static ApiCallLogView instance;
  private final JTextArea log = new JTextArea(32, 64);
  private final JTextField searchTextField = new JTextField(16);
  private final JLabel matchesLabel = new JLabel();
  private final ArrayList<Integer> searchMatchList = new ArrayList<>();
  private int matchIndex;

  private ApiCallLogView() {
    setLayout(new BorderLayout());
    add(new JScrollPane(log), BorderLayout.CENTER);

    JPanel searchPane = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JButton next = new JButton(Icons.getInstance().getIcon(Icons.ICON_DOWN_OPEN, 16, DARK_BG));
    JButton prev = new JButton(Icons.getInstance().getIcon(Icons.ICON_UP_OPEN, 16, DARK_BG));
    JPanel panel = new JPanel();
    panel.setBackground(Color.white);
    panel.add(searchTextField);
    searchTextField.setBorder(BorderFactory.createEmptyBorder());
    panel.add(matchesLabel);
    panel.setBorder(BorderFactory.createLineBorder(Color.lightGray, 1));
    searchPane.add(panel);
    searchPane.add(Box.createHorizontalStrut(8));

    searchPane.add(next);
    searchPane.add(prev);
    searchPane.add(Box.createHorizontalStrut(8));
    add(searchPane, BorderLayout.NORTH);

    searchTextField.getDocument().addDocumentListener(new DefaultDocumentListener(
        e -> {
          getAllMatchIndices(searchTextField.getText());
          nextMatch();
        }));

    next.addActionListener(e -> nextMatch());
    prev.addActionListener(e -> prevMatch());
  }

  private void nextMatch() {
    if (searchMatchList.isEmpty()) {
      return;
    }

    if (++matchIndex == searchMatchList.size()) {
      matchIndex = 0;
    }

    int pos = searchMatchList.get(matchIndex);
    matchesLabel.setText(String.format("%s/%s", matchIndex + 1, searchMatchList.size()));
    highlight(pos, searchTextField.getText().length());
  }

  private void prevMatch() {
    if (searchMatchList.isEmpty()) {
      return;
    }

    if (--matchIndex < 0) {
      matchIndex = searchMatchList.size() - 1;
    }
    int pos = searchMatchList.get(matchIndex);
    matchesLabel.setText(String.format("%s/%s", matchIndex + 1, searchMatchList.size()));
    highlight(pos, searchTextField.getText().length());
  }

  private void highlight(int pos, int findLength) {
    try {
      Highlighter highlighter = log.getHighlighter();
      Highlighter.HighlightPainter painter =
          new DefaultHighlighter.DefaultHighlightPainter(Color.pink);
      Rectangle2D viewRect = log.modelToView2D(pos);
      log.scrollRectToVisible(viewRect.getBounds());
      highlighter.addHighlight(pos, pos + findLength, painter);
      log.setCaretPosition(pos);
      log.moveCaretPosition(pos);
    } catch (BadLocationException ex) {
      ex.printStackTrace();
    }
  }

  private void getAllMatchIndices(String text) {
    matchesLabel.setText("");
    searchMatchList.clear();
    matchIndex = -1;
    log.getHighlighter().removeAllHighlights();

    if (text.length() == 0) {
      return;
    }

    int findLength = text.length();
    int pos = 0;

    while (pos + findLength <= log.getDocument().getLength()) {
      String match = null;
      try {
        match = log.getDocument().getText(pos, findLength);
      } catch (BadLocationException e) {
        e.printStackTrace();
      }

      if (text.equals(match)) {
        searchMatchList.add(pos);
      }
      pos++;
    }

    matchesLabel.setText(String.format("%s/%s", 0, searchMatchList.size()));
  }

  public static ApiCallLogView getInstance() {
    if (instance == null) {
      instance = new ApiCallLogView();
    }
    return instance;
  }

  public void append(String msg) {
    log.append(msg);
    log.setCaretPosition(log.getDocument().getLength());
  }

  public void scrollToEnd() {
    log.append(System.lineSeparator());
    log.append("---");
    log.append(System.lineSeparator());
    log.setCaretPosition(log.getDocument().getLength());
  }
}
