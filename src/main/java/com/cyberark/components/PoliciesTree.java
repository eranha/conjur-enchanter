package com.cyberark.components;

import com.cyberark.models.ResourceIdentifier;
import com.cyberark.models.ResourceType;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.util.HashMap;
import java.util.List;

public class PoliciesTree extends JTree {
  private final HashMap<String, DefaultMutableTreeNode> nodes = new HashMap<>();

  public PoliciesTree(List<ResourceIdentifier> policies) {
    setModel(policies);
  }

  public void setModel(List<ResourceIdentifier> policies) {
    setCellRenderer(new ResourceTreeTreeCellRenderer());

    if (policies.stream().anyMatch(i -> i.getType() != ResourceType.policy)) {
      throw new IllegalArgumentException("input list contains non-policy type resources");
    }

    // populate the policies as tree nodes
    policies.forEach(policy ->
        nodes.put(policy.getId(), new DefaultMutableTreeNode(policy)));

    if (!nodes.containsKey("root")) {
      throw new IllegalArgumentException("root policy not provided");
    }

    policies.forEach(i -> {
        if (i.getId().split("/").length == 1) {
          if (!i.getId().equals("root")) {
            nodes.get("root").add(nodes.get(i.getId()));
          }
        } else {
          String parentId = i.getId().substring(0, i.getId().lastIndexOf('/'));
          nodes.get(parentId).add(nodes.get(i.getId()));
        }
      }
    );

    DefaultTreeModel treeModel = new DefaultTreeModel(nodes.get("root"));
    setModel(treeModel);
  }

  public ResourceIdentifier getSelectedPolicy() {
    return getLastSelectedPathComponent() != null
        ? (ResourceIdentifier) ((DefaultMutableTreeNode) getLastSelectedPathComponent()).getUserObject()
        : null;
  }
}
