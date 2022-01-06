package com.cyberark.components;

import com.cyberark.models.Annotation;
import com.cyberark.models.table.AbstractEditableTableModel;
import com.cyberark.models.table.AnnotationsTableModel;
import lombok.AccessLevel;
import lombok.Getter;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.*;

public class AnnotationsTable extends JPanel {
  @Getter(AccessLevel.PUBLIC)
  private AnnotationsTableModel model;

  private EditableTableImpl<Annotation> tableAnnotations;

  public AnnotationsTable() {
    this(AbstractEditableTableModel.EditMode.ReadOnly, new Annotation[0]);
  }

  public AnnotationsTable(AbstractEditableTableModel.EditMode editMode) {
    this(editMode, new Annotation[0]);
  }

  public AnnotationsTable(AbstractEditableTableModel.EditMode editMode, Annotation[] annotations) {
    this(new AnnotationsTableModel(editMode, annotations));
  }

  public AnnotationsTable(AnnotationsTableModel model) {
    this.model = model;
    initializeComponents();
  }

  private void initializeComponents() {
    tableAnnotations = new EditableTableImpl<>(
        model, this::getAnnotation);

    tableAnnotations.getTable().setToolTipText(
        "<html>Annotations on a user resource are optional and customizable.<br>" +
            "Custom annotations provide a way to store meta data about a resource.<br>Annotations are useful for<br" +
            "human users and automated processing. Conjur API calls can retrieve annotation values from the <br>" +
            "Conjur database.<html/>"
    );

    setLayout(new BorderLayout());
    add(tableAnnotations);
  }

  private Annotation getAnnotation(TableModel model) {
    return new Annotation(
        String.format("annotation_%s", model.getRowCount() + 1),
        String.format("value_%s", model.getRowCount() + 1),
        null
    );
  }
}
