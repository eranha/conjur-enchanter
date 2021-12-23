package com.cyberark.components;

import com.cyberark.Util;
import com.cyberark.models.PolicyModel;
import com.cyberark.models.ResourceIdentifier;
import com.cyberark.models.ResourceType;
import com.cyberark.views.Icons;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
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
  private final List<Point> highlights = new ArrayList<>();


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

  public void addPolicyTextAreaFocusListener(FocusListener listener) {
    policyTextArea.addFocusListener(listener);
  }

  public void removeAllPolicyTextHighlights() {
    policyTextArea.getHighlighter().removeAllHighlights();
  }

  void highlightPlaceHoldersInPolicy() {
    char[] chars = policyTextArea.getText().trim().toCharArray();
    Point currentPoint = null;
    highlights.clear();


    for (int i = 0; i < chars.length; i++) {
      if (chars[i] == '<' || chars[i] == '>') {
        if (currentPoint != null) {
          currentPoint.y = i + 1;
          highlights.add(currentPoint);
          currentPoint = null;
        }

        if (chars[i] == '<') {
          currentPoint = new Point(i, i);
        }
      }
    }

    highlightTextInPolicy(highlights);
  }

  public void highlightTextInPolicy(Set<String> words) {
    String text = policyTextArea.getText().trim();


    policyTextArea.getHighlighter().removeAllHighlights();

    if (Objects.nonNull(highlights)) {
      highlights.clear();
    }

    words.forEach(s -> {
      int index = 0;

      while ((index = text.indexOf(s, index)) > -1 && index <= text.length()) {
        highlights.add(new Point(index, index + s.length()));
        if (index + s.length() + 1 <= text.length()) {
          index += s.length() + 1;
        } else {
          break;
        }
      }
    });

    highlightTextInPolicy(highlights);
  }

  private void highlightTextInPolicy(List<Point> positions) {
    Highlighter highlighter = policyTextArea.getHighlighter();
    Highlighter.HighlightPainter painter =
        new DefaultHighlighter.DefaultHighlightPainter(Color.pink);

    final int currentCaretPosition = policyTextArea.getCaretPosition();

    positions.stream()
        .filter(p -> currentCaretPosition < p.x || currentCaretPosition > p.y)
        .forEach(
        p -> {
          Rectangle viewRect;

          try {
            // viewRect = policyTextArea.modelToView2D(p.x).getBounds();
            // policyTextArea.scrollRectToVisible(viewRect);
            highlighter.addHighlight(p.x, p.y, painter);
          } catch (BadLocationException e) {
            e.printStackTrace();
          }
          // together with caret events this is an endless loop
          // policyTextArea.setCaretPosition(pos);
          // policyTextArea.moveCaretPosition(pos);
        }
    );
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

    copy.addActionListener(ae -> {
      String selectedText = policyTextArea.getSelectedText();
      Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      clipboard.setContents(
          new StringSelection(
              (selectedText == null
                ? policyTextArea.getText().trim()
                : selectedText)
      ), null);
    });

    clear.addActionListener(ae -> policyTextArea.setText(""));

    menu.add(copy);
    menu.add(clear);

    menu.show(policyTextArea, e.getX(),e.getY());
  }

  private Component createPolicyStatementButtonsPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

    panel.add(new JLabel("Policy Statement:"), BorderLayout.WEST);
    panel.add(btnPanel, BorderLayout.CENTER);
    panel.setBorder(BorderFactory.createEmptyBorder(0,0,0,4));

    btnPanel.add(Box.createVerticalBox());
    btnPanel.add(createPolicyButton(ResourceType.policy));
    btnPanel.add(createPolicyButton(ResourceType.user));
    btnPanel.add(createPolicyButton(ResourceType.host));
    btnPanel.add(createPolicyButton(ResourceType.group));
    btnPanel.add(createPolicyButton(ResourceType.layer));
    btnPanel.add(createPolicyButton(ResourceType.variable));
    btnPanel.add(createPolicyButton(ResourceType.webservice));

    JButton buttonHostFactory = new JButton(Icons.getInstance().getIcon(Icons.ICON_HOST_ROTATOR, 16, DARK_BG));
    buttonHostFactory.addActionListener(e -> appendLineToPolicy(hostFactoryFragment()));
    buttonHostFactory.setToolTipText("<html>Add a <b>Host Factory</b> Policy Statement");

    btnPanel.add(buttonHostFactory);

    JButton buttonGrant = new JButton(Icons.getInstance().getIcon(Icons.ICON_UP_OPEN, 16, DARK_BG));
    buttonGrant.addActionListener(e -> appendLineToPolicy(grantFragment()));
    buttonGrant.setToolTipText("<html>Add a <b>Grant</b> Policy Statement</html>");
    btnPanel.add(buttonGrant);

    JButton buttonRevoke = new JButton(Icons.getInstance().getIcon(Icons.ICON_DOWN_OPEN, 16, DARK_BG));
    buttonRevoke.addActionListener(e -> appendLineToPolicy(revokeFragment()));
    buttonRevoke.setToolTipText("<html>Add a <b>Revoke</b> Policy Statement</html>");
    btnPanel.add(buttonRevoke);

    JButton buttonPermission = new JButton(Icons.getInstance().getIcon(Icons.ICON_OK, 16, DARK_BG));
    buttonPermission.addActionListener(e -> appendLineToPolicy(permissionFragment()));
    buttonPermission.setToolTipText("<html>Add a <b>Permit</b> Policy Statement</html>");
    btnPanel.add(buttonPermission);

    JButton buttonDenyPermission = new JButton(Icons.getInstance().getIcon(Icons.ICON_CANCEL, 16, DARK_BG));
    buttonDenyPermission.addActionListener(e -> appendLineToPolicy(denyPermissionFragment()));
    buttonDenyPermission.setToolTipText("<html>Add a <b>Deny</b> Policy Statement</html>");
    btnPanel.add(buttonDenyPermission);

    return panel;
  }

  private void appendLineToPolicy(String line) {
    if (policyTextArea.getText().trim().length() > 0) {
      policyTextArea.append(System.lineSeparator());
    }

    policyTextArea.append(line);
    highlightPlaceHoldersInPolicy();
  }

  private JButton createPolicyButton(ResourceType type) {
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
        fragment = actorRoleFragment(type, elementCount);
        break;
      case group:
        fragment = groupFragment(elementCount);
        break;
      case layer:
        fragment = layerFragment(elementCount);
        break;
      case policy:
        fragment = policyFragment(elementCount);
        break;
      case variable:
        fragment = variableFragment(elementCount);
        break;
      case webservice:
        fragment = webserviceFragment(elementCount);
        break;
    }

    if (Objects.nonNull(fragment)) {
      appendLineToPolicy(fragment);
    }
  }

  private String permissionFragment() {
    String fragment = "- !permit%s" +
        "  role: !<kind-of-role> <role-name>%s" +
        "  privileges: [x, y, z]%s" +
        "  role: !<kind-of-role> <role-name>";

    return String.format(
        fragment,
        System.lineSeparator(),
        System.lineSeparator(),
        System.lineSeparator()
    );
  }

  private String denyPermissionFragment() {
    String fragment = "- !deny%s" +
        "  role: !<kind-of-role> <role-name>%s" +
        "  privileges: [x, y, z]%s" +
        "  role: !<kind-of-role> <role-name>";

    return String.format(
        fragment,
        System.lineSeparator(),
        System.lineSeparator(),
        System.lineSeparator()
    );
  }

  private String revokeFragment() {
    String fragment = "- !revoke%s" +
        "  role: !<kind-of-role> <role-name>%s" +
        "  member: !<kind-of-role> <role-name>";

    return String.format(
        fragment,
        System.lineSeparator(),
        System.lineSeparator()
    );
  }

  private String grantFragment() {
    String fragment = "- !grant%s" +
        "  role: !<kind-of-role> <role-name>%s" +
        "  members:%s" +
        "    - !<kind-of-role> <role-name>%s" +
        "    - !<kind-of-role> <role-name>";

    return String.format(
        fragment,
        System.lineSeparator(),
        System.lineSeparator(),
        System.lineSeparator(),
        System.lineSeparator()
    );
  }

  private String webserviceFragment(int elementCount) {
    String fragment = "- !webservice%n" +
        "  id: webservice%s%n" +
        "  owner: !<kind-of-role> <role-name>%n" +
        "  annotations:%n" +
        "    <key>: <value>";

    return String.format(
        fragment,
        elementCount
    );
  }

  private String groupFragment(int elementCount) {
    String fragment = "- !group%n" +
        "  id: group%s%n" +
        "  owner: !<kind-of-role> <role-name>%n" +
        "  annotations:%n" +
        "    editable: true | false";

    return String.format(
        fragment,
        elementCount
    );
  }

  private String variableFragment(int elementCount) {
    String fragment = "- !variable%n" +
        "  id: variable%s%n" +
        "  kind: <description>%n" +
        "  mime_type:%n" +
        "  annotations:%n" +
        "    <key>: <value>";

    return String.format(
        fragment,
        elementCount
    );
  }

  private String layerFragment(int elementCount) {
    String fragment = "- !layer%n" +
        "  id: layer%s%n" +
        "  owner: !<kind-of-role> <role-name>%n" +
        "  annotations:%n" +
        "    <key>: <value>%n";

    return String.format(
        fragment,
        elementCount
    );
  }

  private String actorRoleFragment(ResourceType type, int elementCount) {
    String fragment = "- !%s%n" +
        "  id: %s%s%n" +
        "  owner: !<kind-of-role> <role-name>%n" +
        "  annotations:%n" +
        "    <key>: <value>%n" +
        "  restricted_to: <network range>";

    return String.format(
        fragment,
            type,
            type,
            elementCount
        );
  }

  private String policyFragment(int elementCount) {
    String fragment = "- !policy%n" +
        "  id: policy%s%n" +
        "  owner: !<kind-of-role> <role-name>%n" +
        "  body:%n" +
        "    <statements>%n";

    return String.format(
        fragment,
        elementCount
    );
  }

  private String hostFactoryFragment() {
    int elementCount = policyTextArea.getDocument().getDefaultRootElement().getElementCount();
    String fragment = "- !host-factory%n" +
        "  id: host-factory%s%n" +
        "  owner: !<kind-of-role> <role-name>%n" +
        "  layers: [ !layer <layer-name>, !layer <layer-name> ]%n" +
        "  annotations:%n" +
        "    <key>: <value>";

    return String.format(
        fragment,
        elementCount
    );
  }

  private void initPolicyTextArea() {
    policyTextArea = new JTextArea(12,24);
    policyTextArea.getDocument().addDocumentListener(new DefaultDocumentListener(_e -> {
      if (propertyChangeListener != null) {
        propertyChangeListener.propertyChange(
            new PropertyChangeEvent(this, POLICY_TEXT, null,
                policyTextArea.getText()));
      }
    }));

    policyTextArea.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        super.mouseClicked(e);
        if (e.isPopupTrigger() || e.getButton() == MouseEvent.BUTTON3) {
          showTextPopupMenu(e);
        }
      }
    });

    policyTextArea.addCaretListener(e -> {
      if (!highlights.isEmpty()) {
        policyTextArea.getHighlighter().removeAllHighlights();
        highlightTextInPolicy(highlights);
      }
    });
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
