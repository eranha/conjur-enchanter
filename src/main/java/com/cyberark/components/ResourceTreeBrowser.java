package com.cyberark.components;

import com.cyberark.models.ResourceIdentifier;
import com.cyberark.models.ResourceModel;
import com.cyberark.models.ResourceType;
import com.cyberark.views.Icons;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.*;

import static com.cyberark.Consts.DARK_BG;
import static javax.swing.tree.TreeSelectionModel.SINGLE_TREE_SELECTION;

public class ResourceTreeBrowser extends JPanel {
  private final Highlighter.HighlightPainter painter =
      new DefaultHighlighter.DefaultHighlightPainter(Color.pink);
  private final  Map<ResourceIdentifier, List<Point>> resourceIndices = new HashMap<>();
  private final Map<ResourceIdentifier, String> policyToText;
  private final Map<ResourceIdentifier, List<ResourceIdentifier>> policyToResources;
  private JTextArea policyTextArea;

  public ResourceTreeBrowser(
      List<ResourceModel> policies,
      Map<ResourceIdentifier, List<ResourceIdentifier>> policyToResources,
      Map<ResourceIdentifier, String> policyToText) {
    super(new BorderLayout());
    this.policyToText = policyToText;
    this.policyToResources = policyToResources;
    initializeComponents(policies);
  }

  private void initializeComponents(List<ResourceModel> policies) {
    ResourceTree resourceTree = new ResourceTree(policies, policyToResources);
    JPanel contentPane = new JPanel(new BorderLayout());

    resourceTree.getSelectionModel().setSelectionMode(SINGLE_TREE_SELECTION);
    resourceTree.getSelectionModel().addTreeSelectionListener(this::handleTreeSelectionEvent);

    JTextField search = new JTextField();
    initSearchTextField(resourceTree, search);

    JPanel searchPane = new JPanel(new BorderLayout());
    searchPane.add(search, BorderLayout.CENTER);
    searchPane.add(new JLabel(Icons.getInstance().getIcon(Icons.SEARCH_ICON_UNICODE, 16, DARK_BG)),
        BorderLayout.EAST);
    searchPane.setBorder(BorderFactory.createEmptyBorder(0,0, 8,4));
    contentPane.add(searchPane, BorderLayout.NORTH);

    JPanel treePanel = new JPanel(new BorderLayout());
    JScrollPane scrollPane = new JScrollPane(resourceTree);
    treePanel.add(scrollPane, BorderLayout.CENTER);
    treePanel.setBorder(BorderFactory.createEmptyBorder(0, 4,0,6));
    contentPane.add(treePanel, BorderLayout.CENTER);
    contentPane.setPreferredSize(new Dimension(640, 480));
    contentPane.setMaximumSize(new Dimension(640, 480));

    JPanel textPanel = new JPanel(new BorderLayout());
    policyTextArea = new JTextArea(5,20);
    textPanel.setBorder(BorderFactory.createEmptyBorder(0,0,0, 8));
    textPanel.add(new JScrollPane(policyTextArea), BorderLayout.CENTER);

    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, contentPane, textPanel);
    splitPane.setDividerLocation(280);
    splitPane.setBorder(BorderFactory.createEmptyBorder());
    add(splitPane, BorderLayout.CENTER);
    resourceTree.setSelectionPath(new TreePath(resourceTree.getRootNode()));
  }

  private void handleTreeSelectionEvent(TreeSelectionEvent e) {
    TreePath selectionPath = ((TreeSelectionModel) e.getSource()).getSelectionPath();

    if (Objects.nonNull(selectionPath)) {
      DefaultMutableTreeNode selectedNode =
          (DefaultMutableTreeNode) selectionPath.getLastPathComponent();

      if (Objects.nonNull(selectedNode)) {
        ResourceIdentifier resource = (ResourceIdentifier) selectedNode.getUserObject();
        ResourceIdentifier policy = (resource.getType() == ResourceType.policy)
            ? resource
            : getResourcePolicy(resource);

        String policyText =  policyToText.get(policy);

        policyTextArea.setText(policyText);
        policyTextArea.getHighlighter().removeAllHighlights();
        highlightResource(resource, policyText);
      }
    }
  }

  private ResourceIdentifier getResourcePolicy(ResourceIdentifier resource) {
    return policyToResources
        .keySet()
        .stream()
        .filter(k -> policyToResources.get(k).contains(resource))
        .findFirst()
        .orElseThrow();
  }

  private void highlightResource(ResourceIdentifier resource, String policy) {
    resourceIndices.computeIfAbsent(resource, v -> getIndices(resource.getId(), policy));

    for (Point highlight : resourceIndices.get(resource)) {
      Highlighter highlighter = policyTextArea.getHighlighter();

      policyTextArea.setCaretPosition(highlight.x);

      // scroll to text
      Rectangle2D viewRect;

      try {
        highlighter.addHighlight(highlight.x, highlight.y, painter);
        viewRect = policyTextArea.modelToView2D(highlight.x);

        if (Objects.nonNull(viewRect)) {
          policyTextArea.scrollRectToVisible(viewRect.getBounds());
        }
      } catch (BadLocationException ex) {
        ex.printStackTrace();
      }
    }
  }

  private List<Point> getIndices(String query, String text) {
    ArrayList<Point> indices = new ArrayList<>();
    int index = 0;

    while (index != -1 && index < text.length()) {
      index = text.indexOf(String.format(" %s%n", query), index);

      if (index > -1) {
        indices.add(new Point(index + 1, index + 1 + query.length()));
        index += query.length() + 1;
      }
    }

    return indices;
  }

  private void initSearchTextField(ResourceTree resourceTree, JTextField searchTextField) {
    searchTextField.getDocument().addDocumentListener(new DefaultDocumentListener(event -> {
      DefaultMutableTreeNode node = null;

      try {

        String search = event.getDocument().getText(0, event.getDocument().getLength());
        node = resourceTree.findFirst(search);
      } catch (BadLocationException ex) {
        ex.printStackTrace();
      }
      if (node != null) {
        resourceTree.selectNode(node);
      } else {
        resourceTree.clearSelection();
      }
    }));
  }
}
