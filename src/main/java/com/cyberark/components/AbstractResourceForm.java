package com.cyberark.components;

import com.cyberark.dialogs.InputDialog;
import com.cyberark.models.Annotation;
import com.cyberark.views.ResourceFormView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.function.Supplier;

abstract class AbstractResourceForm extends ContainerBase implements ResourceFormView {
  protected PropertyChangeListener propertyChangeListener;

  public void setPropertyChangeListener(PropertyChangeListener listener) {
    propertyChangeListener = listener;
  }

  @Override
  public String getId() {
    return null;
  }

  @Override
  public String getOwner() {
    return null;
  }

  @Override
  public String getPolicy() {
    return null;
  }

  @Override
  public List<Annotation> getAnnotations() {
    return null;
  }

  public Component getComponent() {
    return this;
  }

  public int showDialog(Window owner, String title) {
    return showDialog(owner, title, () -> false);
  }

  public int showDialog(Window owner, String title, Supplier<Boolean> enableOkButton) {

    InputDialog dlg = new InputDialog(
        owner,
        title,
        true,
        this,
        enableOkButton.get());
    dlg.addWindowFocusListener(new WindowAdapter() {
      @Override
      public void windowGainedFocus(WindowEvent e) {
        super.windowGainedFocus(e);
        dialogGainedFocus(e);
      }
    });
    return dlg.showDialog();
  }

  protected void dialogGainedFocus(WindowEvent e) {
  }

  protected JButton getDefaultButton() {
    Window windowAncestor = SwingUtilities.getWindowAncestor(this);

    return (windowAncestor instanceof JDialog)
        ? ((JDialog)windowAncestor).getRootPane().getDefaultButton()
        : null;
  }
}
