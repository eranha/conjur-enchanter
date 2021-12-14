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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
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
  private JPanel policyTextButtonsPanel;


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
    policyTextArea.getHighlighter().removeAllHighlights();
  }

  public void highlightTextInPolicy(Set<String> words) {
    int pos = 0;
    String text = policyTextArea.getText().trim();
    Highlighter highlighter = policyTextArea.getHighlighter();
    Highlighter.HighlightPainter painter =
        new DefaultHighlighter.DefaultHighlightPainter(Color.pink);

    policyTextArea.getHighlighter().removeAllHighlights();
    ArrayList<Point> positions = new ArrayList<>();

    words.forEach(s -> {
      int index = 0;

      while ((index = text.indexOf(s, index)) > -1 && index <= text.length()) {
        positions.add(new Point(index, index + s.length()));
        if (index + s.length() + 1 <= text.length()) {
          index += s.length() + 1;
        } else {
          break;
        }
      }
    });

    positions.forEach(
      p -> {
        Rectangle viewRect;

        try {
          viewRect = policyTextArea.modelToView2D(p.x).getBounds();
          policyTextArea.scrollRectToVisible(viewRect);
          highlighter.addHighlight(p.x, p.y, painter);
        } catch (BadLocationException e) {
          e.printStackTrace();
        }

        policyTextArea.setCaretPosition(pos);
        policyTextArea.moveCaretPosition(pos);
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
    createButtonsPanel();

    add(policyTextButtonsPanel,
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

  private void createButtonsPanel() {
    policyTextButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    policyTextButtonsPanel.add(createPolicyButton(ResourceType.policy));
    policyTextButtonsPanel.add(createPolicyButton(ResourceType.user));
    policyTextButtonsPanel.add(createPolicyButton(ResourceType.host));
    policyTextButtonsPanel.add(createPolicyButton(ResourceType.group));
    policyTextButtonsPanel.add(createPolicyButton(ResourceType.layer));
    policyTextButtonsPanel.add(createPolicyButton(ResourceType.variable));
    policyTextButtonsPanel.add(createPolicyButton(ResourceType.webservice));

    JButton buttonHostFactory = new JButton(Icons.getInstance().getIcon(Icons.ICON_HOST_ROTATOR, 16, DARK_BG));
    buttonHostFactory.addActionListener(e -> appendLineToPolicy(hostFactoryFragment()));
    buttonHostFactory.setToolTipText("Host Factory");

    policyTextButtonsPanel.add(buttonHostFactory);

    JButton buttonGrant = new JButton(Icons.getInstance().getIcon(Icons.LOCK_ICON_UNICODE, 16, DARK_BG));
    buttonGrant.addActionListener(e -> appendLineToPolicy(grantFragment()));
    buttonGrant.setToolTipText("Grant");
    policyTextButtonsPanel.add(buttonGrant);

    JButton buttonPermission = new JButton(Icons.getInstance().getIcon(Icons.ICON_SPIN, 16, DARK_BG));
    buttonPermission.addActionListener(e -> appendLineToPolicy(permissionFragment()));
    buttonPermission.setToolTipText("Permit");
    policyTextButtonsPanel.add(buttonPermission);
  }

  private String permissionFragment() {
    String fragment = "- !permit%s" +
        "  role: !<kind-of-role> <role-name>%s" +
        "  privileges: [ x, y, z]%s" +
        "  role: !<kind-of-role> <role-name>";

    return String.format(
        fragment,
        System.lineSeparator(),
        System.lineSeparator(),
        System.lineSeparator()
    );
  }

  private String grantFragment() {
    String fragment = "- !grant%s" +
        "  role: !<kind-of-role> <role-name> #Granting role.%s" +
        "  members:                          #Recipient roles.%s" +
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

  private void appendLineToPolicy(String line) {
    if (policyTextArea.getText().trim().length() > 0) {
      policyTextArea.append(System.lineSeparator());
    }

    policyTextArea.append(line);
  }

  private JButton createPolicyButton(ResourceType type) {
    JButton button = new JButton(Icons.getInstance().getIcon(type, 16, DARK_BG));
    button.addActionListener(e -> appendResourceLineToPolicy(type));
    button.setToolTipText(Util.resourceTypeToTitle(type));
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

    appendLineToPolicy(fragment);
  }

  private String webserviceFragment(int elementCount) {
    String fragment = "- !webservice%n" +
        "  id: webservice%s%n" +
        "  owner: !<kind-of-role> <role-name>%n" +
        "  annotations:%n";

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
        "  layers: [ !layer <layer-name>, ... ]%n" +
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
  }

  private void initPolicyBranchTree() {
    policyBranchTree = new PoliciesTree(
        policyModels
            .stream()
            .map(i -> ResourceIdentifier.fromString(i.id))
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
