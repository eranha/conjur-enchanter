package com.cyberark.actions.resource;

import com.cyberark.Consts;
import com.cyberark.actions.ActionType;
import com.cyberark.components.EditableTableImpl;
import com.cyberark.components.Form;
import com.cyberark.dialogs.InputDialog;
import com.cyberark.exceptions.ResourceAccessException;
import com.cyberark.models.Annotation;
import com.cyberark.models.ResourceIdentifier;
import com.cyberark.models.ResourceModel;
import com.cyberark.models.ResourceType;
import com.cyberark.models.table.AnnotationsTableModel;
import com.cyberark.views.Icons;

import javax.swing.table.TableModel;
import java.util.function.Supplier;

import static com.cyberark.Consts.DARK_BG;

@SelectionBasedAction
public class EditAnnotationsAction<T extends ResourceModel> extends ActionBase<T> {
  public EditAnnotationsAction(Supplier<T> selectedResource) {
    this(
        getString("edit.annotations.action.text"),
        ActionType.EditAnnotations,
        selectedResource
    );
  }

  protected EditAnnotationsAction(String text, ActionType type, Supplier<T> selectedResource) {
    super(text, type, selectedResource);
    putValue(SMALL_ICON, Icons.getInstance().getIcon(Icons.ICON_EDIT,
        16,
        DARK_BG));
    putValue(SHORT_DESCRIPTION, getString("edit.annotations.action.short.description"));
    setEnabled(false);
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
        getString("edit.annotations.action.form.title"),
        getResourcesInfo().getProperty("annotations")
            + getString("edit.annotations.action.note"),
        annotationsTable
    );

    if (InputDialog.showModalDialog(
        getMainForm(),
        String.format(
            getString("edit.annotations.action.dialog.title"),
            resource.getId()
        ),
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

  @Override
  public void setEnabled(boolean enabled) {
    if (enabled && getSelectedResource().getIdentifier().getType() == ResourceType.policy) {
      super.setEnabled(false);
    } else {
      super.setEnabled(enabled);
    }
  }

  private Annotation getAnnotation(TableModel model) {
    return new Annotation(
        String.format(getString("edit.annotations.new.item.default.name"), model.getRowCount() + 1),
        String.format(getString("edit.annotations.new.item.default.value"), model.getRowCount() + 1),
        null
    );
  }
}
