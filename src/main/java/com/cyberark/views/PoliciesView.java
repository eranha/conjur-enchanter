package com.cyberark.views;

import com.cyberark.components.DataTable;
import com.cyberark.components.TextComparer;
import com.cyberark.components.TitlePanel;
import com.cyberark.models.PolicyModel;
import com.cyberark.models.PolicyVersion;
import com.cyberark.models.ViewModel;
import com.cyberark.models.table.PolicyTableModel;
import com.cyberark.models.table.PolicyVersionTableModel;
import com.cyberark.models.table.ResourceTableModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import static com.cyberark.Consts.CYBR_BLUE;

public class PoliciesView extends ResourceViewImpl<PolicyModel> {
  private JTable policyVersionsTable;

  public PoliciesView() {
    super(ViewType.Policies);
  }

  @Override
  public void setModel(ViewModel model) {
    super.setModel(model);
    if (getModel().getRowCount() == 0) {
      PolicyVersionTableModel policyVersionsTableModel = new PolicyVersionTableModel(new PolicyVersion[0]);
      policyVersionsTable.setModel(policyVersionsTableModel);
    }
  }

  // TODO add resource permissions view
  @Override
  protected void initializeComponents() {
    super.initializeComponents();
    setLayout(new BorderLayout());

    policyVersionsTable = new DataTable();

    getResourceTable().getSelectionModel().addListSelectionListener(event -> {
      if (event.getFirstIndex() < 0 ||  getResourceTable().getSelectedRow() < 0) return;
      PolicyTableModel model = (PolicyTableModel) getResourceTable().getModel();
      PolicyVersionTableModel vModel = new PolicyVersionTableModel(model.getPolicyVersion(
          getResourceTable().getSelectedRow()
      ));
      policyVersionsTable.setModel(vModel);
    });

    TitlePanel topPanel = new TitlePanel("Policies", new JScrollPane(getResourceTable()), CYBR_BLUE);
    TitlePanel bottomPanel = new TitlePanel("Policy Versions",
        new JScrollPane(policyVersionsTable), CYBR_BLUE);

    policyVersionsTable.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent mouseEvent) {
        JTable table =(JTable) mouseEvent.getSource();
        Point point = mouseEvent.getPoint();
        int row = table.rowAtPoint(point);
        if (mouseEvent.getClickCount() == 2 && table.getSelectedRow() != -1) {
          PolicyVersionTableModel model = (PolicyVersionTableModel)policyVersionsTable.getModel();
          JOptionPane.showMessageDialog(
              SwingUtilities.getWindowAncestor(PoliciesView.this),
              new JScrollPane(new JTextArea(model.getPolicyVersion(table.getSelectedRow()))),
              "Policy Text",
              JOptionPane.INFORMATION_MESSAGE
          );
        }
      }
    });

    policyVersionsTable.getSelectionModel().addListSelectionListener(e -> {
      DefaultListSelectionModel m = (DefaultListSelectionModel) e.getSource();
      if (m.getSelectedIndices().length == 2) {
        PolicyVersionTableModel model = (PolicyVersionTableModel)policyVersionsTable.getModel();
        TextComparer c = new TextComparer();
        c.compare(model.getPolicyVersion(m.getSelectedIndices()[0]),model.getPolicyVersion(m.getSelectedIndices()[1]));
      }
    });

    JSplitPane splitPane = new JSplitPane(SwingConstants.HORIZONTAL);
    splitPane.setDividerLocation(300);
    splitPane.setLeftComponent(topPanel);
    splitPane.setRightComponent(bottomPanel);
    add(splitPane, BorderLayout.CENTER);
  }

  protected ResourceTableModel<PolicyModel> createTableModel(List<PolicyModel> items) {
    return new PolicyTableModel(items);
  }
}
