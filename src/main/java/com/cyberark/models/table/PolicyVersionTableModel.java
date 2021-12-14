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
        return model.id;
      case 1:
        return model.version;
      case 2:
        return model.role;
      case 3:
        return model.created_at;
      case 4:
        return model.policy_text;
      case 5:
        return model.policy_sha256;
      case 6:
        return model.finished_at;
      case 7:
        return model.client_ip;
    }

    return null;
  }

  public String getPolicyVersion(int rowIndex) {
    return policyVersions[rowIndex].policy_text;
  }
}
