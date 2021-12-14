package com.cyberark.views;

import com.cyberark.actions.ActionType;
import com.cyberark.models.DataModel;
import com.cyberark.models.ResourceModel;
import com.cyberark.models.ViewModel;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

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
