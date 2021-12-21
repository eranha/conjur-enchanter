package com.cyberark.actions;

import com.cyberark.Consts;
import com.cyberark.components.EditableTableImpl;
import com.cyberark.components.Form;
import com.cyberark.dialogs.InputDialog;
import com.cyberark.exceptions.ResourceAccessException;
import com.cyberark.models.Annotation;
import com.cyberark.models.ResourceIdentifier;
import com.cyberark.models.ResourceModel;
import com.cyberark.models.table.AnnotationsTableModel;

import javax.swing.table.TableModel;
import java.util.function.Supplier;

public class EditAnnotationsAction<T extends ResourceModel> extends ActionBase<T> {
  public EditAnnotationsAction(Supplier<T> selectedResource) {
    this("Edit Annotations...", ActionType.EditAnnotations, selectedResource);
  }

  protected EditAnnotationsAction(String text, ActionType type, Supplier<T> selectedResource) {
    super(text, type, selectedResource);
  }

  @Override
  protected void actionPerformed(ResourceModel selectedResource) {
    ResourceIdentifier resource = selectedResource.getIdentifier();
    AnnotationsTableModel annotationsTableModel = new AnnotationsTableModel(
        AnnotationsTableModel.EditMode.AddOnly,
        selectedResource.getAnnotations()
    );

    EditableTableImpl<Annotation> annotationsTable = new EditableTableImpl<>(
        annotationsTableModel, this::getAnnotation);

    Form form = new Form(
        "Annotations",
        getResourcesInfo().getProperty("annotations")
            + " <br><span style='background-color: yellow'>Note:</span> Annotations cannot be deleted.",
        annotationsTable
    );

    if (InputDialog.showDialog(
        getMainForm(),
        String.format("Edit Annotations of %s",
            resource.getId()
        ),
        true,
        form) == InputDialog.OK_OPTION) {
      try {
        selectedResource.setAnnotations(annotationsTableModel.getAnnotations());
        getResourcesService().loadPolicy(Consts.PolicyApiMode.Patch,
            ResourceUtil.getResourcePolicy(selectedResource),
            ResourceIdentifier.fromString(selectedResource.getPolicy()).getId());
        fireEvent(selectedResource);
      } catch (ResourceAccessException e) {
        showErrorDialog(e);
      }
    }
  }

  private Annotation getAnnotation(TableModel model) {
    return new Annotation(
        String.format("annotation_%s", model.getRowCount() + 1),
        String.format("value_%s", model.getRowCount() + 1),
        null
    );
  }
}
