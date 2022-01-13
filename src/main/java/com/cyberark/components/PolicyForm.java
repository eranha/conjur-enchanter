package com.cyberark.components;

import com.cyberark.Util;
import com.cyberark.models.PolicyModel;
import com.cyberark.models.ResourceType;
import com.cyberark.views.PolicyFormView;
import com.cyberark.views.ViewFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class PolicyForm extends AbstractResourceForm implements PolicyFormView {
  private JTextField filePathTextField;
  private final PolicyEditorPane policyEditorPane;
  private PolicyApiMode policyApiMode = PolicyApiMode.Post;

  public PolicyForm(List<PolicyModel> policyModels) {
    policyEditorPane = new PolicyEditorPane(policyModels, null);
    policyEditorPane.setPropertyChangeListener(this::policyTextChangeEvent);
    initializeComponents();
  }

  private void policyTextChangeEvent(PropertyChangeEvent e) {
    if (PolicyEditorPane.POLICY_TEXT.equals(e.getPropertyName()) && getDefaultButton() != null) {
      getDefaultButton().setEnabled(Util.nonNullOrEmptyString(e.getNewValue()));
      validatePolicyText();
    }
  }

  private void validatePolicyText() {
    policyEditorPane.removeAllPolicyTextHighlights();
    policyEditorPane.setPolicyTextAreaTooltipText(null);

    String policyText = policyEditorPane.getPolicyText();

    if (policyApiMode == PolicyApiMode.Post) {
      List<String> list = Arrays.stream((new String[]{"!delete", "!revoke", "!deny"})).collect(Collectors.toList());
      HashSet<String> set = new HashSet<>();

      list.forEach (i -> {
          if (policyText.contains(i)) {
            set.add(i);
          }
        }
      );

      if (set.size() > 0) {
        policyEditorPane.highlightWordsInPolicy(set);
        policyEditorPane.setPolicyTextAreaTooltipText(getString("policy.form.post.mode.tooltip"));
      } else {
        policyEditorPane.highlightPlaceHoldersInPolicy();
      }
    } else {
      policyEditorPane.highlightPlaceHoldersInPolicy();
    }
  }

  private void initializeComponents() {
    setMinimumSize(new Dimension(240,160));
    setPreferredSize(new Dimension(800,600));
    JLabel fileLabel = new JLabel(getString("policy.form.label.file"));
    JLabel apiModeLabel = new JLabel(getString("policy.form.label.mode"));
    filePathTextField = new JTextField(24);
    JButton openFileChooserButton = new JButton("...");
    Insets labelInsets = new Insets(0,0,0,16);

    Container contentPanel = this;
    contentPanel.setLayout(new GridBagLayout());

    openFileChooserButton.setToolTipText(getString("policy.form.select.file.tooltip"));

    // line - file path
    add(apiModeLabel,
        new GridBagConstraints(
            0, 0, 1, 1, 0, 0,
            GridBagConstraints.CENTER, GridBagConstraints.NONE,
            labelInsets, 0, 0
        )
    );

    add( getPolicyApiModeComboBox(),
        new GridBagConstraints(
            1, 0, 2, 1, 1, 0,
            GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
            new Insets(0,0,0,0), 0, 0
        )
    );

    // line - file path
    add(fileLabel,
        new GridBagConstraints(
            0, 1, 1, 1, 0, 0,
            GridBagConstraints.WEST, GridBagConstraints.NONE,
            labelInsets, 0, 0
        )
    );

    add(filePathTextField,
        new GridBagConstraints(
            1, 1, 1, 1, 1, 0,
            GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
            new Insets(0,0,0,0), 0, 0
        )
    );

    add(openFileChooserButton,
        new GridBagConstraints(
            2, 1, 1, 1, 0, 0,
            GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL,
            new Insets(0,0,0,0), 0, 0
        )
    );

    // line - policy editor
    add(policyEditorPane,
        new GridBagConstraints(
            0, 2, 3, 1, 1, 1,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0,0,0,0), 0, 0
        )
    );

    openFileChooserButton.addActionListener(this::handleFileSelectedEvent);
  }

  private JComboBox<String> getPolicyApiModeComboBox() {
    JComboBox<String> policyApiModeComboBox = new JComboBox<>(
        new String[] {
            getString("policy.form.mode.post"),
            getString("policy.form.mode.patch"),
            getString("policy.form.mode.put")
        }
    );

    policyApiModeComboBox.addActionListener(
        e -> {
          policyApiMode = PolicyApiMode.values()[policyApiModeComboBox.getSelectedIndex()];
          validatePolicyText();
        }
    );
    return policyApiModeComboBox;
  }

  private void handleFileSelectedEvent(ActionEvent e) {
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
    int result = fileChooser.showOpenDialog(SwingUtilities.getWindowAncestor(this));

    if (result == JFileChooser.APPROVE_OPTION) {
      File selectedFile = fileChooser.getSelectedFile();
      Path fileName = Path.of(selectedFile.getAbsolutePath());
      try {
        filePathTextField.setText(fileName.toString());
        policyEditorPane.setPolicyText(Files.readString(fileName));
      } catch (IOException ex) {
        ex.printStackTrace();
        ViewFactory.getInstance().getMessageView().showMessageDialog(
            ex.toString(),
            getString("policy.form.load.file.error"),
            JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  @Override
  protected void dialogGainedFocus(WindowEvent e) {
    super.dialogGainedFocus(e);
    policyEditorPane.setFocusInPolicyTextArea();
  }

  @Override
  public String getPolicyText() {
    return policyEditorPane.getPolicyText();
  }

  @Override
  public void setPolicyText(String policyText) {
    policyEditorPane.setPolicyText(policyText);
    validatePolicyText();
  }

  @Override
  public String getBranch() {
    return policyEditorPane.getPolicyBranch();
  }

  @Override
  public void setBranch(String branch) {
  }

  @Override
  public PolicyApiMode getPolicyApiMode() {
    return policyApiMode;
  }

  @Override
  public ResourceType getResourceType() {
    return ResourceType.policy;
  }
}
