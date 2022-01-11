package com.cyberark.controllers;

import com.cyberark.exceptions.ResourceAccessException;
import com.cyberark.models.ResourceModel;
import com.cyberark.views.ResourceView;
import com.cyberark.views.ViewType;

import java.util.function.Consumer;

public interface ResourceViewController {
  ResourceView getResourceView(ViewType viewType);
  void setResourceViewModel(ViewType view) throws ResourceAccessException;
  void setViewType(ViewType viewType);
  void clearData();
  void setSelectionListener(Consumer<ResourceModel> consumer);
}

