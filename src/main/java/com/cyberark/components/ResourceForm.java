package com.cyberark.components;

import com.cyberark.Util;
import com.cyberark.dialogs.InputDialog;
import com.cyberark.models.Annotation;
import com.cyberark.models.ResourceIdentifier;
import com.cyberark.models.ResourceType;
import com.cyberark.models.table.AnnotationsTableModel;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ResourceForm extends AbstractResourceForm {
  public static final String ID_PROPERTY_NAME = "id";
  protected final AnnotationsTableModel annotationsTableModel;
  protected final List<ResourceIdentifier> resources;
  private final ResourceType resourceType;
  private final JTextField textFieldId;
  private String resourceOwner;
  private String resourcePolicy;

  public ResourceForm(ResourceType resourceType,
                      List<ResourceIdentifier> resources,
                      List<ResourceIdentifier> owners) {
    this.resources = resources;
    this.resourceType = resourceType;
    annotationsTableModel = new AnnotationsTableModel(true);
    textFieldId = createIdTextField();
    initializeComponents(owners);
  }

  protected void initializeComponents(List<ResourceIdentifier> roles) {
    setLayout(new GridBagLayout());

    JComboBox<ResourceIdentifier> listOwners = createOwnersComboBox(roles);


    listOwners.setRenderer(new ResourceListItemCellRenderer());
    listOwners.setToolTipText("<html>Optional. If owner is not provided,<br>" +
        "a resource inherits its owner from its policy.</html>");

    int gridy = 0;

    addResourceIdTextField(gridy++);
    addSpacingRow(gridy++);
    addComponentRow(gridy++, "Owner:", listOwners);
    addSpacingRow(gridy++);
    addComponentRow(gridy++, "Policy:", getPolicyPanel());
    addSpacingRow(gridy++);
    addAnnotationsTable(gridy++);
    addSpacingRow(gridy++);

    ComponentViewInfo row = getAdditionalComponentRow();

    if (row != null) {
      addComponentRow(gridy, row.label, row.component,
          row.labelAnchor,
          row.labelFill,
          row.componentFill,
          row.componentWeightX,
          row.componentWeightY,
          row.compSize);
    }
  }

  protected void addAnnotationsTable(int gridy) {
    EditableTable<Annotation> tableAnnotations = new EditableTable<>(
        annotationsTableModel, this::getAnnotation);

    tableAnnotations.getTable().setToolTipText(
        "<html>Annotations on a user resource are optional and customizable.<br>" +
            "Custom annotations provide a way to store meta data about a resource.<br>Annotations are useful for<br" +
            "human users and automated processing. Conjur API calls can retrieve annotation values from the <br>" +
            "Conjur database.<html/>"
    );
    addComponentRow(gridy, "Annotations:",
        tableAnnotations,
        GridBagConstraints.NORTH,
        GridBagConstraints.HORIZONTAL,
        GridBagConstraints.BOTH, 1, 1,
        new Dimension(256, 128));
  }

  protected void addResourceIdTextField(int gridy) {
    addComponentRow(gridy, "ID:", textFieldId);
  }

  protected JPanel getPolicyPanel() {
    JPanel policyPanel = new JPanel(new BorderLayout());
    JPanel policyButtonPanel = new JPanel();
    JTextField policyLabel = new JTextField();
    JButton policyChooserButton = new JButton("...");

    policyChooserButton.setPreferredSize(new Dimension(16, 16));
    policyChooserButton.setMaximumSize(new Dimension(16, 16));
    policyLabel.setEnabled(false);

    policyPanel.add(policyLabel, BorderLayout.CENTER);
    policyButtonPanel.add(policyChooserButton);
    policyPanel.add(policyButtonPanel, BorderLayout.EAST);
    policyChooserButton.addActionListener(e -> setSelectedPolicyBranch(policyLabel));
    return policyPanel;
  }

  private void setSelectedPolicyBranch(JTextField policyLabel) {
    PoliciesTree tree = new PoliciesTree(
        resources
            .stream()
            .filter(i -> i.getType() == ResourceType.policy)
            .collect(Collectors.toList()));
    JScrollPane scrollPane = new JScrollPane(tree);
    scrollPane.setPreferredSize(new Dimension(320, 240));

    if (InputDialog.showDialog(SwingUtilities.getWindowAncestor(this),
        "Select Policy Branch",true,
        scrollPane) == InputDialog.OK_OPTION) {
        ResourceIdentifier id = tree.getSelectedPolicy();
        String policy = id != null
            ? id.getId()
            : null;
        policyLabel.setText(policy);
        setPolicy(policy);
    }
  }

  private void setPolicy(String resourcePolicy) {
    this.resourcePolicy = resourcePolicy;
  }

  private JTextField createIdTextField() {
    JTextField textFieldId = new JTextField();
    textFieldId.getDocument().addDocumentListener(new DefaultDocumentListener(e ->
        onResourceIdTextChange(textFieldId.getText().trim())));
    textFieldId.setToolTipText("<html><b>Required</b>. Identifies the user name. This is the Conjur login name.<br>" +
        "It should not contain special characters such as \":\" or \"/\".<br>It may contain the @ symbol.</html>");

    suggestedResourceName(textFieldId);
    return textFieldId;
  }

  private void suggestedResourceName(JTextField textFieldId) {
    Set<String> ids = resources
        .stream()
        .filter(i -> i.getType() == resourceType).map(ResourceIdentifier::getId)
        .collect(Collectors.toSet());
    int nameIndex = ids.size() + 1;

    String suggestedResourceName = String.format(
        "%s%s",
        resourceType,
        nameIndex);

    while (ids.contains(suggestedResourceName)) {
      suggestedResourceName = String.format(
          "%s%s",
          resourceType,
          ++nameIndex);
    }

    textFieldId.setText(suggestedResourceName);
  }

  private void onResourceIdTextChange(String text) {
    if (propertyChangeListener != null) {
      propertyChangeListener.propertyChange(
          new PropertyChangeEvent(this, ID_PROPERTY_NAME, null, text));
    }

    if (getDefaultButton() != null) {
      getDefaultButton().setEnabled(Util.stringIsNotNullOrEmpty(text));
    }
  }

  private JComboBox<ResourceIdentifier> createOwnersComboBox(List<ResourceIdentifier> resources) {
    JComboBox<ResourceIdentifier> listOwners = new JComboBox<>();
    DefaultComboBoxModel<ResourceIdentifier> model = new DefaultComboBoxModel<>();
    model.addElement(ResourceIdentifier.fromString("account:policy:Inherit"));
    resources.forEach(model::addElement);
    listOwners.setModel(model);

    listOwners.addItemListener(e ->
        setResourceOwner(listOwners.getSelectedIndex() == 0 || resources.isEmpty()
            ? null
            : resources.get(listOwners.getSelectedIndex() - 1).getFullyQualifiedId()));

    return listOwners;
  }

  private void setResourceOwner(String owner) {
    this.resourceOwner = owner;
  }

  protected ComponentViewInfo getAdditionalComponentRow() {
    return null;
  }

  protected void addComponentRow(int gridy, String text, Component comp) {
    addComponentRow(gridy, text, comp,
        GridBagConstraints.WEST,
        GridBagConstraints.HORIZONTAL,
        GridBagConstraints.HORIZONTAL, 0, 0, null);
  }

  protected void addComponentRow(int gridY, String text, Component comp, int labelAnchor, int labelFill,
                               int componentFill, int componentWeightX, int componentWeightY,
                               Dimension compSize) {
    GridBagConstraints lc = new GridBagConstraints();
    GridBagConstraints sc = new GridBagConstraints();
    GridBagConstraints cc = new GridBagConstraints();
    JLabel label = new JLabel(text);

    label.setLabelFor(comp);
    lc.gridy = gridY;
    lc.fill = labelFill;
    lc.anchor = labelAnchor;
    add(label, lc);

    sc.gridx = 1;
    sc.gridy = gridY;

    add(Box.createRigidArea(new Dimension(16, 1)), sc);

    if (compSize != null) {
      comp.setPreferredSize(compSize);
    }

    cc.gridy = gridY;
    cc.gridx = 2;
    cc.fill = componentFill;
    cc.anchor = GridBagConstraints.NORTHWEST;
    cc.weightx = componentWeightX;
    cc.weighty = componentWeightY;
    add(comp, cc);
  }

  private void addSpacingRow(int gridy) {
    GridBagConstraints c = new GridBagConstraints();
    c.gridy = gridy;
    add(Box.createRigidArea(new Dimension(1, 8)), c);
  }

  private Annotation getAnnotation(TableModel model) {
    return new Annotation(
        String.format("annotation_%s", model.getRowCount() + 1),
        String.format("value_%s", model.getRowCount() + 1),
        null
    );
  }

  @Override
  public String getId() {
    return textFieldId.getText().trim();
  }

  @Override
  public String getOwner() {
    return resourceOwner;
  }

  @Override
  public String getPolicy() {
    return resourcePolicy;
  }

  @Override
  public List<Annotation> getAnnotations() {
    return Arrays.asList(annotationsTableModel.getAnnotations());
  }

  @Override
  public ResourceType getResourceType() {
    return resourceType;
  }
}
