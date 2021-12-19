package com.cyberark.controllers;

import com.cyberark.exceptions.ResourceAccessException;
import com.cyberark.views.ResourceView;
import com.cyberark.views.ViewType;

public interface ResourceViewController {
  ResourceView getResourceView(ViewType viewType);

  void setResourceViewModel(ViewType view) throws ResourceAccessException;

  void setViewType(ViewType viewType);

  void clearData();
}

