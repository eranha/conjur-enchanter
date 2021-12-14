package com.cyberark.components;

import com.cyberark.models.ResourceIdentifier;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.*;
import java.util.List;

public class ResourceTree extends JTree {
  private final HashMap<ResourceIdentifier, DefaultMutableTreeNode> nodes = new HashMap<>();
  private DefaultMutableTreeNode root;
  private final HashSet<ResourceIdentifier> resourceIds = new HashSet<>();

  public ResourceTree(Map<ResourceIdentifier, List<ResourceIdentifier>> model) {
    setCellRenderer(new ResourceTreeTreeCellRenderer());

    model.forEach((id, value) -> {
      resourceIds.add(id);
      DefaultMutableTreeNode node = new DefaultMutableTreeNode(id);
      nodes.put(id, node);
      if (id.getId().equals("root")) {
        root = node;
      }
      value.forEach(i -> {
        DefaultMutableTreeNode child = new DefaultMutableTreeNode(i);
        node.add(child);
        resourceIds.add(i);
        nodes.put(i, child);
      });
    });

    model.keySet().forEach(id -> {
      String policy = id.getId();

      if (policy.split("/").length == 1) {
        if (!policy.equals("root")) {
          root.add(nodes.get(id));
        }
      } else {
        String fullyQualifiedId = id.getFullyQualifiedId();
        ResourceIdentifier parent = ResourceIdentifier.fromString(
            fullyQualifiedId.substring(0, fullyQualifiedId.lastIndexOf('/')));
        nodes.get(parent).add(nodes.get(id));
      }
    });

    DefaultTreeModel treeModel = new DefaultTreeModel(root);
    setModel(treeModel);
  }

  public DefaultMutableTreeNode findFirst(String str) {
    ResourceIdentifier id = resourceIds.stream()
        .filter(i -> getResourceIdExcludingPath(i.getId()).startsWith(str)).findFirst().orElse(null);
    return id == null ? null : nodes.get(id);
  }

  @Override
  public void setSelectionPath(TreePath path) {
    super.setSelectionPath(path);
    Rectangle bounds = getPathBounds(path);
    // set the height to the visible height to force the node to top
    Objects.requireNonNull(bounds).height = getVisibleRect().height;
    scrollRectToVisible(bounds);
  }

  private String getResourceIdExcludingPath(String id) {
    if (id.indexOf('/') < 0) return id;
    String s = id.substring(id.lastIndexOf('/') + 1);
    System.out.println(s);
    return s;
  }
}
