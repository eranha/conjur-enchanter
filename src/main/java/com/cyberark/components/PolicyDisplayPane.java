package com.cyberark.components;

import com.cyberark.models.PolicyModel;
import com.cyberark.models.ResourceModel;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.stream.Collectors;


public class PolicyDisplayPane extends ContainerBase {
  public static final String RESOURCE_ID = "resourceId";
  private final List<PolicyModel> policyModels;
  private PropertyChangeListener propertyChangeListener;
  private boolean copyPermissions;
  private JTextField nameTextField;
  private JTextArea policyTextArea;
  private PoliciesTree policiesTree;

  public PolicyDisplayPane(String name, List<PolicyModel> policyModels, String policyText) {
    this.policyModels = policyModels;
    initializeComponents(name);
    policyTextArea.setText(policyText);
  }

  public void setPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
    this.propertyChangeListener = propertyChangeListener;
  }

  private void initializeComponents(String name) {
    JLabel nameLabel = new JLabel(getString("required.id.label.text"));
    nameTextField = new RequiredTextField(name);
    Container contentPanel = this;
    JCheckBox checkBox = new JCheckBox(getString("copy.permissions.label.text"));

    setMinimumSize(new Dimension(240,160));
    setPreferredSize(new Dimension(480,320));
    contentPanel.setLayout(new GridBagLayout());
    checkBox.addActionListener(e -> setCopyPermissions(checkBox.isSelected()));
    nameTextField.getDocument().addDocumentListener(new DefaultDocumentListener(e -> fireEvent()));

    // line - policy text
    add(nameLabel,
        new GridBagConstraints(
        0, 0, 1, 0, 0, 0,
          GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
          new Insets(4,0,0,0), 0, 0
        )
    );

    add(nameTextField,
        new GridBagConstraints(
            1, 0, 1, 1, 0, 0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
            new Insets(0,13, 8,4), 0, 0
        )
    );

    // line - checkbox copy permissions
    add(checkBox,
        new GridBagConstraints(
        1, 1, 1, 1, 0, 0,
          GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
          new Insets(0,8, 8,0), 0, 0
        )
    );

    // line - policy text
    add(new JLabel(getString("text.label.text")),
        new GridBagConstraints(
            0, 2, 1, 0, 0, 0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
            new Insets(0,0,0,0), 0, 0
        )
    );

    policyTextArea = new JTextArea();
    add(new JScrollPane(policyTextArea),
        new GridBagConstraints(
            1, 2, 1, 1, 1, 1,
            GridBagConstraints.NORTH, GridBagConstraints.BOTH,
            new Insets(0,16, 8,8), 0, 0
        )
    );

    // line - policy Branch
    add(new JLabel(getString("branch.label.text")),
        new GridBagConstraints(
            0, 3, 1, 0, 0, 0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
            new Insets(0,0,0,0), 0, 0
        )
    );

    policiesTree = new PoliciesTree(
        policyModels
            .stream()
            .map(ResourceModel::getIdentifier)
            .collect(Collectors.toList())
    );

    add(new JScrollPane(policiesTree),
        new GridBagConstraints(
            1, 3, 1, 1, 1, 0.5,
            GridBagConstraints.SOUTH, GridBagConstraints.BOTH,
            new Insets(0,16, 8,8), 0, 0
        )
    );
  }

  private void fireEvent() {
    if (propertyChangeListener != null) {
      propertyChangeListener.propertyChange(
          new PropertyChangeEvent(this, RESOURCE_ID, null, nameTextField.getText()));
    }
  }

  private void setCopyPermissions(boolean selected) {
    copyPermissions = selected;
  }

  public void setPolicyText(String policy) {
    policyTextArea.setText(policy);
  }

  public boolean isCopyPermissions() {
    return copyPermissions;
  }

  public String getPolicyText() {
    return policyTextArea.getText().trim();
  }

  public String getBranch() {
    return policiesTree.getSelectedPolicy().getId();
  }

  public String getResourceId() {
    return nameTextField.getText().trim();
  }
}
