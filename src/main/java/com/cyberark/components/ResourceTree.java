package com.cyberark.components;

import com.cyberark.models.ResourceIdentifier;
import com.cyberark.models.ResourceModel;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.List;
import java.util.*;

public class ResourceTree extends JTree {
  private final HashMap<ResourceIdentifier, DefaultMutableTreeNode> nodes = new HashMap<>();
  private DefaultMutableTreeNode root;
  private final HashSet<ResourceIdentifier> resourceIds = new HashSet<>();

  public ResourceTree(List<ResourceModel> policies, Map<ResourceIdentifier, List<ResourceIdentifier>> model) {
    setCellRenderer(new ResourceTreeTreeCellRenderer());
    addPolicyNodes(policies);

    // add resource child nodes of each policy
    model.forEach((policy, value) -> {
      DefaultMutableTreeNode parentPolicyNode = nodes.get(policy);

      value.forEach(i -> {
        DefaultMutableTreeNode child = new DefaultMutableTreeNode(i);
        parentPolicyNode.add(child);
        resourceIds.add(i);
        nodes.put(i, child);
      });
    });

    DefaultTreeModel treeModel = new DefaultTreeModel(root);
    setModel(treeModel);
  }

  private void addPolicyNodes(List<ResourceModel> policies) {
    ResourceModel rootPolicy = policies
        .stream()
        .filter(m -> m.getPolicy() == null)
        .findFirst()
        .orElseThrow();

    addPolicyNode(rootPolicy, policies);
  }

  private void addPolicyNode(ResourceModel policyModel, List<ResourceModel> policies) {
    // add the treeNode
    ResourceIdentifier identifier = policyModel.getIdentifier();
    DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(identifier);

    resourceIds.add(identifier);
    nodes.put(identifier, treeNode);

    if (policyModel.getPolicy() == null) {
      root = treeNode;
    } else {
      ResourceIdentifier parentPolicy = ResourceIdentifier.fromString(policyModel.getPolicy());
      nodes.get(parentPolicy).add(treeNode);
    }

    // filter and add children
    policies
        .stream()
        .filter(m -> policyModel.getId().equals(m.getPolicy()))
        .forEach(p -> addPolicyNode(p, policies));
  }

  public DefaultMutableTreeNode findFirst(String str) {
    ResourceIdentifier id = resourceIds.stream()
        .filter(i -> getResourceIdExcludingPath(i.getId())
        .startsWith(str))
        .findFirst()
        .orElse(null);
    return id == null ? null : nodes.get(id);
  }

  private String getResourceIdExcludingPath(String id) {
    if (id.indexOf('/') < 0) return id;
    return id.substring(id.lastIndexOf('/') + 1);
  }

  public TreeNode getRootNode() {
    return root;
  }

  public void selectNode(DefaultMutableTreeNode node) {
    TreePath path = new TreePath(node.getPath());
    setSelectionPath(path);
    // set the height to the visible height to force the node to top
    Rectangle bounds = getPathBounds(path);
    if (Objects.nonNull(bounds)) {
      bounds.height = getVisibleRect().height;
      scrollRectToVisible(bounds);
    }
  }
}
