package com.cyberark.components;

import com.cyberark.Util;
import com.cyberark.models.Annotation;
import com.cyberark.models.ResourceIdentifier;
import com.cyberark.models.ResourceType;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SecretForm extends ResourceForm {
  private JTextField textFieldKind;
  private JTextField textFieldMimeType;

  public SecretForm(List<ResourceIdentifier> resources) {
    super(ResourceType.variable, resources, null );
    initializeComponents(resources);
  }

  @Override
  protected void initializeComponents(List<ResourceIdentifier> policies) {
    setLayout(new GridBagLayout());
    int row = 0;

    // ID
    addResourceIdTextField(row++);
    addSpacingRow(row++);
    // POLICY
    addComponentRow(row++, "Policy:", getPolicyPanel());
    addSpacingRow(row++);

    // variable specific input KIND
    textFieldKind = new JTextField();
    textFieldKind.setToolTipText("<html>Optional. Assigns a descriptive label to the variable, " +
        "<br>such as password. The values are not predefined; create your own.</html>");
    addComponentRow(row++, "Kind:", textFieldKind);
    addSpacingRow(row++);
    // variable specific input MIME_TYPE
    textFieldMimeType = new JTextField();
    textFieldMimeType.setToolTipText("<html>Optional. The expected MIME type of the values. " +
        "<br>This attribute is used to set the Content-Type header on HTTP responses.</html>");
    addComponentRow(row++, "Mime Type:", textFieldMimeType);
    addSpacingRow(row++);
    // ANNOTATIONS
    addAnnotationsTable(row++);
    addSpacingRow(row);
  }

  private void addSpacingRow(int gridy) {
    GridBagConstraints c = new GridBagConstraints();
    c.gridy = gridy;
    add(Box.createRigidArea(new Dimension(1, 8)), c);
  }

  @Override
  public List<Annotation> getAnnotations() {
    List<Annotation> annotations = new ArrayList<>(Arrays.asList(annotationsTableModel.getAnnotations()));

    if (Util.stringIsNotNullOrEmpty(getKind())) {
      annotations.add(new Annotation("conjur/kind", getKind(), null));
    }

    if (Util.stringIsNotNullOrEmpty(getMimeType())) {
      annotations.add(new Annotation("conjur/mime_type", getMimeType(), null));
    }

    return annotations;
  }

  private String getKind() {
    return textFieldKind.getText().trim();
  }

  private String getMimeType() {
    return textFieldMimeType.getText().trim();
  }

  @Override
  public ResourceType getResourceType() {
    return ResourceType.variable;
  }
}
