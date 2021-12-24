package com.cyberark.components;

import com.cyberark.Util;
import com.cyberark.models.PolicyModel;
import com.cyberark.models.ResourceIdentifier;
import com.cyberark.models.ResourceType;
import com.cyberark.util.PolicyFragments;
import com.cyberark.views.Icons;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

import static com.cyberark.Consts.CYBR_BLUE;
import static com.cyberark.Consts.DARK_BG;

public class PolicyEditorPane extends JPanel {
  public static final String POLICY_TEXT = "policy.text";
  private JTextArea policyTextArea;
  private final List<PolicyModel> policyModels;
  private JTree policyBranchTree;
  private String policyBranch;
  private PropertyChangeListener propertyChangeListener;
  private JLabel policyTextTip;
  private final Map<Point, Object> highlights = new HashMap<>();
  private final Highlighter.HighlightPainter painter =
      new DefaultHighlighter.DefaultHighlightPainter(Color.pink);

  public PolicyEditorPane(List<PolicyModel> policyModels, String policyText) {
    this.policyModels = policyModels;
    initializeComponents();
    policyTextArea.setText(policyText);
  }

  public void setPolicyTextAreaTooltipText(String text) {
    policyTextArea.setToolTipText(text);
    policyTextTip.getParent().setVisible(text != null);
    policyTextTip.setText(text);
  }

  public void removeAllPolicyTextHighlights() {
    clearHighlights();
  }

  void highlightPlaceHoldersInPolicy() {
    char[] chars = policyTextArea.getText().trim().toCharArray();
    Point currentPoint = null;
    clearHighlights();

    for (int i = 0; i < chars.length; i++) {
      if (chars[i] == '<' || chars[i] == '>') {
        if (currentPoint != null) {
          currentPoint.y = i + 1;
          mapHighlightTag(currentPoint, null);
          currentPoint = null;
        }

        if (chars[i] == '<') {
          currentPoint = new Point(i, i);
        }
      }
    }

    highlights.putAll(highlightWordsInPolicy(highlights));
  }

  private void clearHighlights() {
    policyTextArea.getHighlighter().removeAllHighlights();
    highlights.clear();
  }

  public void highlightWordsInPolicy(Set<String> words) {
    String text = policyTextArea.getText().trim();

    // reset
    clearHighlights();

    // highlight place holders
    highlightPlaceHoldersInPolicy();

    // map words positions
    words.forEach(s -> {
      int index = 0;

      while ((index = text.indexOf(s, index)) > -1 && index <= text.length()) {
        mapHighlightTag(new Point(index, index + s.length()), null);
        if (index + s.length() + 1 <= text.length()) {
          index += s.length() + 1;
        } else {
          break;
        }
      }
    });

    // append words to highlights map
    highlightWordsInPolicy(highlights).forEach(this::mapHighlightTag);
  }

  private Map<Point, Object> highlightWordsInPolicy(Map<Point, Object> positions) {
    Highlighter highlighter = policyTextArea.getHighlighter();

    Map<Point, Object> tmpHighlights = new HashMap<>();
    final int currentCaretPosition = policyTextArea.getCaretPosition();
    final int selectionStart = policyTextArea.getSelectionStart();
    final int selectionEnd = policyTextArea.getSelectionEnd();

    positions.keySet().stream()
    .filter(p -> currentCaretPosition <= p.x || currentCaretPosition >= p.y)
    .filter(p -> !(selectionStart > p.x && selectionEnd < p.y))
    .forEach(
      p -> {
        try {
          if (highlights.get(p) == null) {
            Object tag = highlighter.addHighlight(p.x, p.y, painter);
            tmpHighlights.put(p, tag);
          }
        } catch (BadLocationException e) {
          e.printStackTrace();
        }
      }
    );

    return tmpHighlights;
  }

  private void initializeComponents() {
    setMinimumSize(new Dimension(240,160));
    setPreferredSize(new Dimension(480,320));
    JLabel fileContentLabel = new JLabel("Text:");
    JLabel branchLabel = new JLabel("Branch:");

    initPolicyTextArea();
    initPolicyBranchTree();

    Container contentPanel = this;
    contentPanel.setLayout(new GridBagLayout());

    // line - buttons
    createPolicyStatementButtonsPanel();

    add(createPolicyStatementButtonsPanel(),
        new GridBagConstraints(
        1, 0, 1, 1, 1, 0,
          GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
          new Insets(2,0, 2,0), 0, 0
        )
    );

    // line - policy text
    add(fileContentLabel,
        new GridBagConstraints(
        0, 1, 1, 0, 0, 0,
            GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
            new Insets(0,0,0,24), 0, 0
        )
    );

    JPanel policyTextPanel = new JPanel(new BorderLayout());
    policyTextPanel.add(new JScrollPane(policyTextArea));

    Icon icon = Icons.getInstance().getIcon(Icons.ICON_INFO, 24, CYBR_BLUE);
    policyTextTip = new JLabel();
    JPanel panel = new JPanel(new BorderLayout());
    panel.add(new JLabel(icon), BorderLayout.WEST);
    panel.add(policyTextTip);
    panel.setVisible(false);
    policyTextPanel.add(panel, BorderLayout.SOUTH);

    add(policyTextPanel,
        new GridBagConstraints(
            1, 1, 1, 1, 1, 1,
            GridBagConstraints.NORTH, GridBagConstraints.BOTH,
            new Insets(0,2, 8,8), 0, 0
        )
    );

    // line - branch
    add(branchLabel,
        new GridBagConstraints(
        0, 2, 0, 0, 0, 0,
          GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
            new Insets(8,2, 8,8), 0, 0
        )
    );

    JScrollPane scrollPane = new JScrollPane(policyBranchTree);
    scrollPane.setPreferredSize(new Dimension(256, 128));
    add(scrollPane,
        new GridBagConstraints(
            1, 2, 1, 1, 1, 1,
            GridBagConstraints.NORTH, GridBagConstraints.BOTH,
            new Insets(8,2, 0,8), 0, 0
        )
    );
  }

  private void showTextPopupMenu(MouseEvent e) {
    JPopupMenu menu = new JPopupMenu();
    JMenuItem copy = new JMenuItem(policyTextArea.getSelectedText() == null ? "Copy" : "Copy Selection");
    JMenuItem clear = new JMenuItem("Clear All");

    copy.addActionListener(ae -> copyPolicyText());
    clear.addActionListener(ae -> clearPolicyText());

    menu.add(copy);
    menu.add(clear);

    menu.show(policyTextArea, e.getX(),e.getY());
  }

  private void clearPolicyText() {
    policyTextArea.setText("");
    clearHighlights();
  }

  private void copyPolicyText() {
    String selectedText = policyTextArea.getSelectedText();
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    clipboard.setContents(
        new StringSelection(
            (selectedText == null
              ? policyTextArea.getText().trim()
              : selectedText)
    ), null);
  }

  private Component createPolicyStatementButtonsPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

    panel.add(new JLabel("Policy Statement:"), BorderLayout.WEST);
    panel.add(btnPanel, BorderLayout.CENTER);
    panel.setBorder(BorderFactory.createEmptyBorder(0,0,0,4));

    btnPanel.add(Box.createVerticalBox());

    Arrays.stream(ResourceType.values()).forEach(t -> btnPanel.add(createPolicyResourceButton(t)));

    final String tooltip = "<html>Add a <b>%s</b> Policy Statement</html>";

    btnPanel.add(
        createButton(Icons.ICON_PLUS,
            e -> appendLineToPolicy(PolicyFragments.grantFragment()), String.format(tooltip, "Grant"))
    );

    btnPanel.add(
        createButton(Icons.ICON_MINUS,
            e -> appendLineToPolicy(PolicyFragments.revokeFragment()),String.format(tooltip, "Revoke"))
    );

    btnPanel.add(
        createButton(Icons.ICON_THUMBS_UP,
            e -> appendLineToPolicy(PolicyFragments.permitFragment()),String.format(tooltip, "Permit"))
    );

    btnPanel.add(
        createButton(Icons.ICON_THUMBS_DOWN,
            e -> appendLineToPolicy(PolicyFragments.denyFragment()),String.format(tooltip, "Deny"))
    );

    btnPanel.add(
        createButton(Icons.ICON_CLONE,
            e -> copyPolicyText(),"Copy")
    );

    btnPanel.add(
        createButton(Icons.ICON_TRASH,
            e -> clearPolicyText(),"Clear All")
    );

    return panel;
  }

  private Component createButton(char iconCode, ActionListener ae, String tooltip) {
    JButton button = new JButton(
      Icons.getInstance().getIcon(iconCode,
          16,
          DARK_BG)
    );

    button.addActionListener(ae);
    button.setToolTipText(tooltip);

    return button;
  }

  private void appendLineToPolicy(String line) {
    if (policyTextArea.getText().trim().length() > 0) {
      policyTextArea.append(System.lineSeparator());
    }

    policyTextArea.append(line);
    policyTextArea.setCaretPosition(policyTextArea.getText().length());

    try {
      Rectangle2D viewRect = policyTextArea.modelToView2D(policyTextArea.getText().length());
      policyTextArea.scrollRectToVisible(viewRect.getBounds());

    } catch (BadLocationException e) {
      e.printStackTrace();
    }
  }

  private JButton createPolicyResourceButton(ResourceType type) {
    JButton button = new JButton(Icons.getInstance().getIcon(type, 16, DARK_BG));
    button.addActionListener(e -> appendResourceLineToPolicy(type));
    button.setToolTipText(
        String.format(
            (type != ResourceType.policy
                ?  "<html>Add a <b>%s</b> Policy Statement</html>"
                : "<html>Add a <b>%s</b> Statement</html>"),
          Util.resourceTypeToTitle(type)
        )
    );
    return button;
  }

  private void appendResourceLineToPolicy(ResourceType type) {
    if (policyTextArea.getText().trim().length() > 0) {
      policyTextArea.append(System.lineSeparator());
    }

    int elementCount = policyTextArea.getDocument().getDefaultRootElement().getElementCount();
    String fragment = null;

    switch (type) {
      case user:
      case host:
        fragment = PolicyFragments.actorRoleFragment(type, elementCount);
        break;
      case group:
        fragment = PolicyFragments.groupFragment(elementCount);
        break;
      case layer:
        fragment = PolicyFragments.layerFragment(elementCount);
        break;
      case policy:
        fragment = PolicyFragments.policyFragment(elementCount);
        break;
      case variable:
        fragment = PolicyFragments.variableFragment(elementCount);
        break;
      case webservice:
        fragment = PolicyFragments.webserviceFragment(elementCount);
        break;
      case host_factory:
        fragment = PolicyFragments.hostFactoryFragment(elementCount);
        break;
    }

    if (Objects.nonNull(fragment)) {
      appendLineToPolicy(fragment);
    }
  }

  private void initPolicyTextArea() {
    policyTextArea = new JTextArea(12,24);
    policyTextArea.getDocument()
        .addDocumentListener(
            new DefaultDocumentListener(_e -> {
              if (propertyChangeListener != null) {
                propertyChangeListener.propertyChange(
                  new PropertyChangeEvent(this, POLICY_TEXT, null,
                    policyTextArea.getText()));
              }
          })
        );

    policyTextArea.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        super.mouseClicked(e);
        if (e.isPopupTrigger() || e.getButton() == MouseEvent.BUTTON3) {
          showTextPopupMenu(e);
        }
      }
    });

    policyTextArea.addCaretListener(this::handlePolicyTextEditorCaretEvent);
  }

  private void handlePolicyTextEditorCaretEvent(CaretEvent e) {
    final JTextArea jt = policyTextArea;

    if (!highlights.isEmpty()) {
      highlights.forEach((p, t) -> {
        boolean caretIsWithinHighlightBounds = e.getDot() > p.x && e.getDot()  < p.y;
        boolean highlightBoundsIntersectsWithSelection = p.x > jt.getSelectionStart() && p.x < jt.getSelectionEnd();

        if (caretIsWithinHighlightBounds || highlightBoundsIntersectsWithSelection) {
         if (highlights.get(p) != null) {
           removeHighlight(p);
         }
       } else {
         if (highlights.get(p) == null) {
           addHighlight(p);
         }
       }
     });

    }
  }



  private void addHighlight(Point p) {
    if (highlights.get(p) != null) return;
    try {
      mapHighlightTag(p, policyTextArea.getHighlighter().addHighlight(p.x, p.y, painter));
    } catch (BadLocationException badLocationException) {
      badLocationException.printStackTrace();
    }
  }

  private void removeHighlight(Point p) {
    if (highlights.get(p) != null) {
      policyTextArea.getHighlighter().removeHighlight(highlights.get(p));
      mapHighlightTag(p, null);
    }
  }

  private void mapHighlightTag(Point p, Object tag) {
    highlights.put(p, tag);
  }

  private void initPolicyBranchTree() {
    policyBranchTree = new PoliciesTree(
        policyModels
            .stream()
            .map(i -> ResourceIdentifier.fromString(i.getId()))
            .collect(Collectors.toList()));
    policyBranchTree.getSelectionModel().setSelectionMode( TreeSelectionModel.SINGLE_TREE_SELECTION );
    policyBranchTree.getSelectionModel().addTreeSelectionListener(e -> setPolicyBranch(
        policyBranchTree.getLastSelectedPathComponent() != null
        ? ((DefaultMutableTreeNode) policyBranchTree.getLastSelectedPathComponent()).getUserObject()
        : null));
  }

  private void setPolicyBranch(Object policyBranch) {

    this.policyBranch = policyBranch instanceof ResourceIdentifier
    ? ((ResourceIdentifier)policyBranch).getId()
    : null ;
  }

  public void setPolicyText(String text) {
    policyTextArea.setText(text);
  }

  public String getPolicyText() {
    return policyTextArea.getText().trim();
  }

  public void setFocusInPolicyTextArea() {
    policyTextArea.requestFocusInWindow();
  }

  public String getPolicyBranch() {
    return policyBranch;
  }

  public void setPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
    this.propertyChangeListener = propertyChangeListener;
  }
}
