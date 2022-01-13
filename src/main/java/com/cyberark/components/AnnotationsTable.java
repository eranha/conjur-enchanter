package com.cyberark.components;

import com.cyberark.models.Annotation;
import com.cyberark.models.table.AbstractEditableTableModel;
import com.cyberark.models.table.AnnotationsTableModel;
import lombok.AccessLevel;
import lombok.Getter;

import javax.swing.table.TableModel;
import java.awt.*;

public class AnnotationsTable extends ContainerBase {
  @Getter(AccessLevel.PUBLIC)
  private AnnotationsTableModel model;

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
    EditableTableImpl<Annotation> tableAnnotations = new EditableTableImpl<>(
        model, this::getAnnotation);

    tableAnnotations.getTable().setToolTipText(
        getString("annotations.table.tooltip")
    );

    setLayout(new BorderLayout());
    add(tableAnnotations);
  }

  private Annotation getAnnotation(TableModel model) {
    return new Annotation(
        String.format(getString("edit.annotations.new.item.default.name"), model.getRowCount() + 1),
        String.format(getString("edit.annotations.new.item.default.value"), model.getRowCount() + 1),
        null
    );
  }
}
