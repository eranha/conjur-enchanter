package com.cyberark.views;

import com.cyberark.components.ResourceForm;

import java.awt.*;
import java.beans.PropertyChangeListener;

/**
 * Interfae to policy form used to populate a resource model.
 */
public interface PolicyFormView extends ResourceFormView {
  String getPolicyText();
  void setPolicyText(String policyText);
  String getBranch();
  void setBranch(String branch);
  PolicyApiMode getPolicyApiMode();
}
