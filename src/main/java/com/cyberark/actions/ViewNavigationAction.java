package com.cyberark.actions;

import com.cyberark.Util;
import com.cyberark.event.Events;
import com.cyberark.models.ResourceType;
import com.cyberark.views.Icons;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static com.cyberark.Consts.CYBR_BLUE;

public class ViewNavigationAction extends AbstractAction {
  private final ActionListener delegate;
  private final ResourceType resourceType;

  public ViewNavigationAction(ResourceType resourceType, ActionListener delegate) {
    super(Util.getViewType(resourceType).toString(),
        Icons.getInstance().getIcon(resourceType, 24, CYBR_BLUE));
    this.delegate = delegate;
    this.resourceType = resourceType;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    delegate.actionPerformed(new ActionEvent(this, Events.NAVIGATE_TO_VIEW, resourceType.toString()));
  }

  public ResourceType getResourceType() {
    return resourceType;
  }
}
