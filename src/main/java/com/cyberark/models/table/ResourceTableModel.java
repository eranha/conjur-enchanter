package com.cyberark.models.table;

import com.cyberark.models.ResourceModel;
import com.cyberark.models.ViewModel;

import javax.swing.table.TableModel;
import java.util.List;

public interface ResourceTableModel<T extends ResourceModel> extends ViewModel, TableModel {
  List<T> getResourceModels();
  T getResourceModel(int rowIndex);
  int getResourceModelIndex(ResourceModel resourceModel);
  void clearData();
}
