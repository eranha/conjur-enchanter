package com.cyberark.components;

import com.cyberark.Consts;
import com.cyberark.models.ResourceModel;
import com.cyberark.views.Icons;

import javax.swing.*;
import java.awt.*;

public class ResourcePolicyView extends ContainerBase {
  private JTextArea permissionsTextArea;
  private JTextArea policyTextArea;
  private JLabel titleLabel;

  public ResourcePolicyView(ResourceModel resource,
                            String policy,
                            String permissions) {
    initializeComponents();
    policyTextArea.setText(policy);
    permissionsTextArea.setText(permissions);
    titleLabel.setIcon(Icons.getInstance().getIcon(resource.getIdentifier().getType(), 24, Consts.DARK_BG));
    titleLabel.setText(resource.getIdentifier().getId());
  }

  private void initializeComponents() {
    setMinimumSize(new Dimension(240,160));
    setPreferredSize(new Dimension(480,320));

    policyTextArea = new JTextArea(12,24);
    permissionsTextArea = new JTextArea(12,24);
    titleLabel = new JLabel();

    JPanel panel = new JPanel(new GridBagLayout());

    setLayout(new BorderLayout());
    add(titleLabel, BorderLayout.NORTH);
    add(panel, BorderLayout.CENTER);

    GridBagConstraints spacerConstraints = new GridBagConstraints(0, 0, 1, 1, 0, 0,
        GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0),
        0, 0);

    panel.add(Box.createVerticalStrut(16), spacerConstraints);

    JPanel panel1 = new JPanel(new BorderLayout());
    panel1.setBorder( BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(),
        getString("resource.policy.view.policy.label")));
    panel1.add(new JScrollPane(policyTextArea), BorderLayout.CENTER);
    panel.add(panel1,
        new GridBagConstraints(0, 1, 1, 1, 1, 2,
          GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(0,0,0,2),
        0, 0
        )
    );

    spacerConstraints.gridy = 2;
    panel.add(Box.createVerticalStrut(16), spacerConstraints);

    JPanel panel2 = new JPanel(new BorderLayout());
    panel2.setBorder( BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(),
        getString("resource.policy.view.permissions.label")));
    panel2.add(new JScrollPane(permissionsTextArea), BorderLayout.CENTER);
    panel.add(panel2,
        new GridBagConstraints(0, 3, 1, 1, 1, 2,
          GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,2),
        0, 0
        )
    );
  }
}
