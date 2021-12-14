package com.cyberark.controllers;

import com.cyberark.views.View;
import com.cyberark.views.ViewType;

import java.awt.event.ActionEvent;

public interface ViewController {
  <T> View getView(ViewType view);

  void setStatusLabel(String msg);

  void reloadView(ViewType view);

  ViewType getCurrentViewType();

  void setView(ViewType view);

  void reloadView();

  void clearAllViews();

  void actionPerformed(ActionEvent actionEvent);
}
