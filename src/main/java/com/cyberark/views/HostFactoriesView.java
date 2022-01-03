package com.cyberark.views;

import com.cyberark.actions.hostfactory.CreateHostAction;
import com.cyberark.actions.hostfactory.CreateTokensAction;
import com.cyberark.actions.hostfactory.RevokeTokensAction;
import com.cyberark.components.DataTable;
import com.cyberark.components.TitlePanel;
import com.cyberark.models.HostFactory;
import com.cyberark.models.HostFactoryToken;
import com.cyberark.models.ResourceIdentifier;
import com.cyberark.models.table.DefaultResourceTableModel;
import com.cyberark.models.table.ResourceTableModel;
import org.ocpsoft.prettytime.PrettyTime;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Date;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import static com.cyberark.Consts.CYBR_BLUE;

public class HostFactoriesView extends ResourceViewImpl<HostFactory> {
  private DefaultListModel<String> layersModel;
  private DefaultTableModel tokensTableModel;

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

  @Override
  protected void populateResourceData(HostFactory resourceModel) {
    super.populateResourceData(resourceModel);
    layersModel.clear();

    Arrays.stream(getSelectedResource().getLayers()).map(
        i -> ResourceIdentifier.fromString(i).getId()).forEach(layersModel::addElement);

    tokensTableModel.setRowCount(0);
    Arrays.stream(getSelectedResource().getTokens())
        .forEach(t -> tokensTableModel.addRow(new Object[] {
            t.token, getFormattedDate(t),
            t.cidr.length > 0 ? Arrays.toString(t.cidr) : null
        }));
  }

  private String getFormattedDate(HostFactoryToken t) {
    Instant exp = Instant.parse( t.expiration );
    PrettyTime pt = new PrettyTime();
    return pt.format(Date.from(exp));
  }

  @Override
  protected Component getInfoPanel() {
    Component infoPane =  super.getInfoPanel();
    layersModel = new DefaultListModel<>();
    tokensTableModel = new DefaultTableModel(
        new Vector<>(Arrays.asList("Token", "Expiration", "CIDR")),
        0) {
      @Override
      public boolean isCellEditable(int row, int column) {
        //all cells false
        return false;
      }
    };

    JSplitPane rightSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    TitlePanel permissionsPanel = new TitlePanel("" +
        "Layers",
        new JScrollPane(new JList<>(layersModel)), CYBR_BLUE);
    TitlePanel annotationsPanel = new TitlePanel(
        "Tokens",
        new JScrollPane(new DataTable(tokensTableModel)), CYBR_BLUE);

    rightSplitPane.setTopComponent(permissionsPanel);
    rightSplitPane.setBottomComponent(annotationsPanel);
    rightSplitPane.setDividerLocation(180);

    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, infoPane, rightSplitPane);
    splitPane.setDividerLocation(640);

    return splitPane;
  }
}
