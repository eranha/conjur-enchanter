package com.cyberark.views;

import com.cyberark.actions.ViewNavigationAction;
import com.cyberark.components.DataTable;
import com.cyberark.components.RoleTableCellRenderer;
import com.cyberark.components.TitlePanel;
import com.cyberark.models.DashboardViewModel;
import com.cyberark.models.ResourceIdentifier;
import com.cyberark.models.ResourceType;
import com.cyberark.models.ViewModel;
import com.cyberark.models.audit.AuditEvent;
import com.cyberark.models.audit.AuditTableModel;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.cyberark.Consts.CYBR_BLUE;

public class DashboardView extends JPanel implements View {
  private JTable table = new DataTable();
  private AuditTableModel auditTableModel;

  public DashboardView(ActionListener actionListener) {
    initializeComponent(actionListener);
  }

  private void initializeComponent(ActionListener actionListener) {
    setLayout(new BorderLayout());
    table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    //table.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);

    table = new DataTable(){
      @Override
      public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        Component component = super.prepareRenderer(renderer, row, column);
        int rendererWidth = component.getPreferredSize().width;
        TableColumn tableColumn = getColumnModel().getColumn(column);
        tableColumn.setPreferredWidth(Math.max(rendererWidth + getIntercellSpacing().width, tableColumn.getPreferredWidth()));
        return component;
      }
    };

    table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
    table.setDefaultRenderer(ResourceIdentifier.class, new RoleTableCellRenderer());

    JPanel buttonsPanel = new JPanel(new GridLayout(1, ResourceType.values().length - 1, 8,8));
    buttonsPanel.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
    Arrays.stream(
        ResourceType.values())
        .filter(t -> t != ResourceType.host_factory)
        .forEach(i -> buttonsPanel.add(createButton(i, actionListener)));

    add(buttonsPanel, BorderLayout.NORTH);
    TitlePanel panel = new TitlePanel("Recent Activity", new JScrollPane(table), CYBR_BLUE);
    add(panel, BorderLayout.CENTER);
  }

  private Map<ResourceType, JButton> dashboardButtons = new HashMap<>();

  private JButton createButton(ResourceType type, ActionListener actionListener) {
    JButton b = new JButton(new ViewNavigationAction(type, actionListener));
    b.setForeground(CYBR_BLUE);
    b.setMinimumSize(new Dimension(64,64));
    b.setPreferredSize(new Dimension(64,64));
    b.setIconTextGap(8);
    dashboardButtons.put(type,b);
    return b;
  }

  @Override
  public void applyFilter(String query) {
    table.setModel(auditTableModel);
    java.util.List<AuditEvent> events =  auditTableModel.getEvents();
    table.setModel(new AuditTableModel(events
        .stream()
        .filter(i -> i.getMessage().contains(query)).collect(Collectors.toList())));
  }

  @Override
  public ViewType getType() {
    return ViewType.Dashboard;
  }

  @Override
  public Component getComponent() {
    return this;
  }

  @Override
  public void setModel(ViewModel model) {
    if (model instanceof DashboardViewModel) {
      DashboardViewModel dashboardViewModel = (DashboardViewModel)model;
      auditTableModel = new AuditTableModel(dashboardViewModel.getAuditEvents());
      table.setModel(auditTableModel);

      dashboardViewModel.getResourceCount().forEach((t, c) -> dashboardButtons.get(t)
          .setText(String.format(
              "<html><font style=\"font-size:11px\">%s</font><br>" +
                  "<center><font style=\"font-size:16px\">%s</font></center></html>",
              dashboardButtons.get(t).getText(), c)));
    }
  }

  @Override
  public void clearData() {
    table.setModel(new AuditTableModel(new ArrayList<>()));
  }
}
