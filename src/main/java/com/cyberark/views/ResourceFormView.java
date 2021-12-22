package com.cyberark.views;

import com.cyberark.Consts;
import com.cyberark.models.Annotation;
import com.cyberark.models.ResourceType;

import java.awt.*;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.function.Supplier;

/**
 * Interfae to all resource forms used to populate a resource model.
 */
public interface ResourceFormView extends Consts {
  String getId();
  String getOwner();
  String getPolicy();
  List<Annotation> getAnnotations();
  void setPropertyChangeListener(PropertyChangeListener listener);
  Component getComponent();
  int showDialog(Window owner, String title);
  int showDialog(Window owner, String title, Supplier<Boolean> enableOkButton);
  ResourceType getResourceType();
}
