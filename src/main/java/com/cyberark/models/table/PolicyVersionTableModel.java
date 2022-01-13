package com.cyberark.models.table;

import com.cyberark.Util;
import com.cyberark.models.PolicyVersion;

import javax.swing.table.AbstractTableModel;

import static com.cyberark.util.Resources.getString;

public class PolicyVersionTableModel extends AbstractTableModel {
  private final PolicyVersion[] policyVersions;

  private final String[] columnNames =  getString("policy.version.table.model.columns").split(",");

  public PolicyVersionTableModel(PolicyVersion[] policyVersions) {
    this.policyVersions = policyVersions;
  }

  @Override
  public int getRowCount() {
    return policyVersions.length;
  }

  @Override
  public String getColumnName(int column) {
    return columnNames[column];
  }

  @Override
  public int getColumnCount() {
    return columnNames.length;
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    PolicyVersion model = policyVersions[rowIndex];

    switch (columnIndex) {
      case 0:
        return model.getId();
      case 1:
        return model.getVersion();
      case 2:
        return model.getRole();
      case 3:
        return Util.prettyDate(model.getCreatedAt());
      case 4:
        return Util.prettyDate(model.getFinishedAt());
      case 5:
        return model.getPolicyText();
      case 6:
        return model.getPolicySha256();
      case 7:
        return model.getClientIp();
    }

    return null;
  }

  public String getPolicyVersion(int rowIndex) {
    return policyVersions[rowIndex].getPolicyText();
  }
}
