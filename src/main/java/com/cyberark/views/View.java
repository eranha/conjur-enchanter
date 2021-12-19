package com.cyberark.views;

import com.cyberark.models.ViewModel;

import java.awt.*;

/**
 * Interfacrt to all main views of the applicaiton.
 */
public interface View {
  void applyFilter(String query);
  ViewType getType();
  Component getComponent();
  void setModel(ViewModel model);
  void clearData();
}
