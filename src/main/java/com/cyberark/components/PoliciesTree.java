package com.cyberark.components;

import com.cyberark.models.ResourceIdentifier;
import com.cyberark.models.ResourceType;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.util.HashMap;
import java.util.List;

import static com.cyberark.Consts.ROOT_POLICY;

public class PoliciesTree extends JTree {
  private final HashMap<String, DefaultMutableTreeNode> nodes = new HashMap<>();

  public PoliciesTree(List<ResourceIdentifier> policies) {
    setModel(policies);
  }

  public void setModel(List<ResourceIdentifier> policies) {
    if (policies == null || policies.isEmpty()) {
      throw new IllegalArgumentException("policies cannot be null or empty");
    }

    if (policies.stream().anyMatch(i -> i.getType() != ResourceType.policy)) {
      throw new IllegalArgumentException("input list contains unexpected non-policy type resources");
    }

    setCellRenderer(new ResourceTreeTreeCellRenderer());


    // populate the policies as tree nodes
    policies.forEach(policy ->
        nodes.put(policy.getId(), new DefaultMutableTreeNode(policy)));

    if (!nodes.containsKey(ROOT_POLICY)) {
      throw new IllegalArgumentException("root policy not not found in policies");
    }

    policies.forEach(i -> {
        if (i.getId().split("/").length == 1) {
          if (!i.getId().equals(ROOT_POLICY)) {
            nodes.get(ROOT_POLICY).add(nodes.get(i.getId()));
          }
        } else {
          String parentId = i.getId().substring(0, i.getId().lastIndexOf('/'));
          nodes.get(parentId).add(nodes.get(i.getId()));
        }
      }
    );

    DefaultTreeModel treeModel = new DefaultTreeModel(nodes.get(ROOT_POLICY));
    setModel(treeModel);
  }

  public ResourceIdentifier getSelectedPolicy() {
    return getLastSelectedPathComponent() != null
        ? (ResourceIdentifier) ((DefaultMutableTreeNode) getLastSelectedPathComponent()).getUserObject()
        : (ResourceIdentifier) nodes.get(ROOT_POLICY).getUserObject();
  }
}
