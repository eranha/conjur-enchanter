package com.cyberark.views;

import com.cyberark.actions.resource.hostfactory.CreateHostAction;
import com.cyberark.actions.resource.hostfactory.CreateTokensAction;
import com.cyberark.actions.resource.hostfactory.RevokeTokensAction;
import com.cyberark.components.DataTable;
import com.cyberark.components.TitlePanel;
import com.cyberark.components.TokensTableCellRenderer;
import com.cyberark.models.ResourceIdentifier;
import com.cyberark.models.hostfactory.HostFactory;
import com.cyberark.models.table.DefaultResourceTableModel;
import com.cyberark.models.table.ResourceTableModel;
import com.cyberark.models.table.TokensTableModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

import static com.cyberark.Consts.CYBR_BLUE;

public class HostFactoriesView extends ResourceViewImpl<HostFactory> {
  private DefaultListModel<String> layersModel;
  private TokensTableModel tokensTableModel;
  private DefaultListModel<String> hostsModel;
  private JTable tokensTable;
  private Timer timer;
  private Instant nextTokenToExpire;

  public HostFactoriesView() {
    super(ViewType.HostFactories);
    timer.start();
  }

  @Override
  public void setVisible(boolean aFlag) {
    super.setVisible(aFlag);
  }

  @Override
  protected void initializeComponents() {
    super.initializeComponents();
    timer = new Timer((int)Duration.of(1, ChronoUnit.SECONDS).toMillis(), this::checkForTokenExpiration);
  }

  private void checkForTokenExpiration(ActionEvent actionEvent) {
    if (nextTokenToExpire != null) {
      if (nextTokenToExpire.isAfter(Instant.now())) return;

      if (Instant.now().isAfter(nextTokenToExpire)) { // did the next_to_expire_token expired?
        // reload tokens table
        tokensTableModel.setTokens(getSelectedResource().getTokens());
        nextTokenToExpire = null;
      }
    }

    HostFactory resource = getSelectedResource();

    if (resource != null) {
      Instant next = Arrays.stream(resource.getTokens())
          .map(t -> Instant.parse(t.getExpiration()))
          .filter(t -> t.isAfter(Instant.now()))  // filter out expired token
          .min(Instant::compareTo)
          .orElse(null);

      if (next != null) {
        if (nextTokenToExpire == null) {
          nextTokenToExpire = next;
        }
      }
    }
  }

  @Override
  public void setResourceTableModel(ResourceTableModel<HostFactory> model) {
    super.setResourceTableModel(model);
    nextTokenToExpire = null;
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

  @Override
  protected void populateResourceData(HostFactory resourceModel) {
    super.populateResourceData(resourceModel);
    nextTokenToExpire = null;
    layersModel.clear();
    hostsModel.clear();

    TokensTableCellRenderer renderer = (TokensTableCellRenderer) tokensTable.getDefaultRenderer(String.class);
    renderer.setTokens(resourceModel.getTokens());

    Arrays.stream(getSelectedResource()
        .getLayers()).map(
          i -> ResourceIdentifier.fromString(i).getId()
        )
        .forEach(layersModel::addElement);

    getSelectedResource().getHosts().forEach(hostsModel::addElement);
    tokensTableModel.setTokens(getSelectedResource().getTokens());
  }

  @Override
  protected Component getInfoPanel() {
    Component infoPane =  super.getInfoPanel();
    layersModel = new DefaultListModel<>();
    hostsModel = new DefaultListModel<>();
    tokensTableModel = new TokensTableModel();

    JSplitPane rightSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);


    TitlePanel layersPanel = new TitlePanel("" +
        "Layers",
        new JScrollPane(new JList<>(layersModel)), CYBR_BLUE);

    TitlePanel hostsPanel = new TitlePanel("" +
        "Hosts",
        new JScrollPane(new JList<>(hostsModel)), CYBR_BLUE);

    JSplitPane layersHostsSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, layersPanel, hostsPanel);
    layersHostsSplitPane.setDividerLocation(90);
    tokensTable = new DataTable(tokensTableModel);
    tokensTable.setDefaultRenderer(
        String.class,
        new TokensTableCellRenderer());

    TitlePanel annotationsPanel = new TitlePanel(
        "Tokens",
        new JScrollPane(tokensTable), CYBR_BLUE);

    rightSplitPane.setTopComponent(layersHostsSplitPane);
    rightSplitPane.setBottomComponent(annotationsPanel);
    rightSplitPane.setDividerLocation(180);

    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, infoPane, rightSplitPane);
    splitPane.setDividerLocation(640);

    return splitPane;
  }
}
