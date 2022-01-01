package com.cyberark.views;

import com.cyberark.actions.hostfactory.CreateHostAction;
import com.cyberark.actions.hostfactory.CreateTokensAction;
import com.cyberark.actions.hostfactory.RevokeTokensAction;
import com.cyberark.models.HostFactory;
import com.cyberark.models.table.DefaultResourceTableModel;
import com.cyberark.models.table.ResourceTableModel;

import javax.swing.*;
import java.util.List;

public class HostFactoriesView extends ResourceViewImpl<HostFactory> {
  public HostFactoriesView() {
    super(ViewType.HostFactories);
  }


  @Override
  protected ResourceTableModel<HostFactory> createTableModel(List<HostFactory> items) {
    return new DefaultResourceTableModel<>(items);
  }

  @Override
  protected List<Action> getMenuActions() {
    List<Action> items = super.getMenuActions();
    items.add(new CreateHostAction(this::getSelectedResource));
    items.add(new CreateTokensAction(this::getSelectedResource));
    items.add(new RevokeTokensAction(this::getSelectedResource));
    return items;
  }
}
