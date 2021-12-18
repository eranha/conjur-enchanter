package com.cyberark.actions;

import com.cyberark.Application;
import com.cyberark.components.DefaultDocumentListener;
import com.cyberark.components.ResourceTree;
import com.cyberark.dialogs.InputDialog;
import com.cyberark.exceptions.ResourceAccessException;
import com.cyberark.models.ResourceIdentifier;
import com.cyberark.resource.ResourceServiceFactory;
import com.cyberark.views.ErrorView;
import com.cyberark.views.Icons;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Map;

import static com.cyberark.Consts.DARK_BG;

public class ShowResourceTreeAction extends AbstractAction {
  public ShowResourceTreeAction() {
    super("Resources");
    putValue(SHORT_DESCRIPTION, "View resources tree");
    putValue(MNEMONIC_KEY, KeyEvent.VK_T);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    final ResourceTree[] tree = new ResourceTree[1];
    Map<ResourceIdentifier, List<ResourceIdentifier>> resources;

    try {
      resources = ResourceServiceFactory
          .getInstance()
          .getResourcesService()
          .getPolicyResources();
      tree[0] = new ResourceTree(resources);
    } catch (ResourceAccessException ex) {
      ex.printStackTrace();
      ErrorView.showErrorMessage(ex);
      return;
    }

    JPanel panel = new JPanel(new BorderLayout());
    JScrollPane scrollPane = new JScrollPane(tree[0]);
    JTextField search = new JTextField();

    search.getDocument().addDocumentListener(new DefaultDocumentListener(event -> {
      DefaultMutableTreeNode node = null;
      System.out.println(event);
      try {
        node = tree[0].findFirst(event.getDocument().getText(0, event.getDocument().getLength()));
      } catch (BadLocationException ex) {
        ex.printStackTrace();
      }
      if (node != null) {
        TreePath path = new TreePath(node.getPath());
        System.out.println("path: " + path);
        tree[0].setSelectionPath(path);
      } else {
        System.out.println("clear");
        tree[0].clearSelection();
      }
    }));

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
    panel.setPreferredSize(new Dimension(480, 320));
    InputDialog.showDialog(
        Application.getInstance().getMainForm(),
         "Resources Browser",
        true,
        panel,
        JOptionPane.OK_OPTION);
  }
}
