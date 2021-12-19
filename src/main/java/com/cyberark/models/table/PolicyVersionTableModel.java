package com.cyberark.models.table;

import com.cyberark.models.PolicyVersion;

import javax.swing.table.AbstractTableModel;

public class PolicyVersionTableModel extends AbstractTableModel {
  private final PolicyVersion[] policyVersions;

  private final String[] columnNames =  {
      "id",
      "version",
      "role",
      "created_at",
      "policy_text",
      "policy_sha256",
      "finished_at",
      "client_ip"};

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
        return model.getCreatedAt();
      case 4:
        return model.getPolicyText();
      case 5:
        return model.getPolicySha256();
      case 6:
        return model.getFinishedAt();
      case 7:
        return model.getClientIp();
    }

    return null;
  }

  public String getPolicyVersion(int rowIndex) {
    return policyVersions[rowIndex].getPolicyText();
  }
}
