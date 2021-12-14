package com.cyberark.views;

import com.cyberark.Consts;
import com.cyberark.models.Annotation;
import com.cyberark.models.ResourceIdentifier;
import com.cyberark.models.ResourceType;

import java.awt.*;
import java.beans.PropertyChangeListener;
import java.util.List;

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
  ResourceType getResourceType();
}
