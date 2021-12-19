package com.cyberark.models.table;

import com.cyberark.models.PolicyModel;
import com.cyberark.models.PolicyVersion;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class PolicyTableModel extends DefaultResourceTableModel<PolicyModel> {
  public PolicyTableModel(List<PolicyModel> policyModel) {
    super(policyModel);
  }

  public PolicyVersion[] getPolicyVersion(int rowIndex) {
    Stream<PolicyVersion> sorted = Arrays.stream(getModel().get(rowIndex).getPolicyVersions())
        .sorted((x, y) -> Integer.compare(y.getVersion(), x.getVersion()));
    return sorted.toArray(PolicyVersion[]::new);
  }
}
