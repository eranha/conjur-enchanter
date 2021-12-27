package com.cyberark.components;

import com.cyberark.models.ResourceIdentifier;
import com.cyberark.views.Icons;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.cyberark.Consts.DARK_BG;
import static javax.swing.tree.TreeSelectionModel.SINGLE_TREE_SELECTION;

public class ResourceTreeBrowser extends JPanel {
  private JTextArea policyTextArea = new JTextArea();
  private final Highlighter.HighlightPainter painter =
      new DefaultHighlighter.DefaultHighlightPainter(Color.pink);

  public ResourceTreeBrowser(Map<ResourceIdentifier, List<ResourceIdentifier>> resources,
                             String policy) {
    initializeComponents(resources, policy);
  }

  private void initializeComponents(Map<ResourceIdentifier, List<ResourceIdentifier>> resources, String policy) {
    ResourceTree resourceTree = new ResourceTree(resources);
    JPanel panel = new JPanel(new BorderLayout());
    JScrollPane scrollPane = new JScrollPane(resourceTree);
    JTextField search = new JTextField();
    policyTextArea = new JTextArea(5,20);
    policyTextArea.setText(policy);

    resourceTree.getSelectionModel().setSelectionMode(SINGLE_TREE_SELECTION);
    resourceTree.getSelectionModel().addTreeSelectionListener(e -> {
      DefaultMutableTreeNode selectedNode =
          (DefaultMutableTreeNode)resourceTree.getLastSelectedPathComponent();
      if (Objects.nonNull(selectedNode)) {
        ResourceIdentifier resource = (ResourceIdentifier) selectedNode.getUserObject();
        int index = policy.indexOf(String.format("id: %s%n", resource.getId()));
        policyTextArea.getHighlighter().removeAllHighlights();
        if (index > -1 && index <= policyTextArea.getText().length()) {
          Highlighter highlighter = policyTextArea.getHighlighter();

          policyTextArea.setCaretPosition(index);
          Rectangle2D viewRect;
          try {
            highlighter.addHighlight(index + 4, index + 4 + resource.getId().length(), painter);
            viewRect = policyTextArea.modelToView2D(index);
            if (Objects.nonNull(viewRect)) {
              policyTextArea.scrollRectToVisible(viewRect.getBounds());
            }
          } catch (BadLocationException ex) {
            ex.printStackTrace();
          }
        }
      }
    });

    setLayout(new BorderLayout());

    initSearchTextField(resourceTree, search);

    JPanel searchPane = new JPanel(new BorderLayout());
    searchPane.add(search, BorderLayout.CENTER);
    searchPane.add(new JLabel(Icons.getInstance().getIcon(Icons.SEARCH_ICON_UNICODE, 16, DARK_BG)),
        BorderLayout.EAST);
    searchPane.setBorder(BorderFactory.createEmptyBorder(0,0, 8,4));
    panel.add(searchPane, BorderLayout.NORTH);

    JPanel treePanel = new JPanel(new BorderLayout());
    treePanel.add(scrollPane, BorderLayout.CENTER);
    treePanel.setBorder(BorderFactory.createEmptyBorder(0, 4,0,6));
    panel.add(treePanel, BorderLayout.CENTER);
    panel.setPreferredSize(new Dimension(640, 480));
    panel.setMaximumSize(new Dimension(640, 480));
    JPanel textPanel = new JPanel(new BorderLayout());
    textPanel.setBorder(BorderFactory.createEmptyBorder(0,0,0, 8));
    textPanel.add(new JScrollPane(policyTextArea), BorderLayout.CENTER);
    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panel, textPanel);
    splitPane.setDividerLocation(280);
    splitPane.setBorder(BorderFactory.createEmptyBorder());
    add(splitPane, BorderLayout.CENTER);
    resourceTree.setSelectionPath(new TreePath(resourceTree.getRootNode()));
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
