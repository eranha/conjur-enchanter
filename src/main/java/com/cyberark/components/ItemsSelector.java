package com.cyberark.components;

import com.cyberark.models.ResourceIdentifier;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Dual list horizontal control, enables select and move items form one list to the other.
 */
public class ItemsSelector extends ContainerBase {
  private final DefaultListModel<ResourceIdentifier> selectedItemsModel = new DefaultListModel<>();
  private final DefaultListModel<ResourceIdentifier> unSelectedItemsModel = new DefaultListModel<>();
  private final JList<ResourceIdentifier> leftList = new JList<>(unSelectedItemsModel);
  private final JList<ResourceIdentifier> rightList = new JList<>(selectedItemsModel);

  public ItemsSelector(List<ResourceIdentifier> items, List<ResourceIdentifier> selectedItems) {
    selectedItems.forEach(selectedItemsModel::addElement);
    initializeComponents(items);
  }

  public void addSelectedItemsListener(Consumer<ListDataEvent> listener) {
    selectedItemsModel.addListDataListener(new ListDataListener() {
      @Override
      public void intervalAdded(ListDataEvent e) {
        listener.accept(e);
      }

      @Override
      public void intervalRemoved(ListDataEvent e) {
        listener.accept(e);
      }

      @Override
      public void contentsChanged(ListDataEvent e) {

      }
    });
  }

  private void initializeComponents(List<ResourceIdentifier> items) {
    setLayout(new GridBagLayout());

    items.forEach(unSelectedItemsModel::addElement);

    leftList.setCellRenderer(new ResourceListItemCellRenderer());
    rightList.setCellRenderer(new ResourceListItemCellRenderer());

    add(getItemsPanel(getString("items.selector.available.label.text"), leftList),
        new GridBagConstraints(
            0, 0, 1, 1, 1, 1,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0,0,0,0), 0, 0
        )
    );

    add(getButtonsPanel(),
        new GridBagConstraints(
            1, 0, 1, 1, 0, 0,
            GridBagConstraints.CENTER, GridBagConstraints.NONE,
            new Insets(0,0,0,0), 0, 0
        )
    );

    add(getItemsPanel(getString("items.selector.selected.label.text"), rightList),
        new GridBagConstraints(
            2, 0, 1, 1, 1, 1,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0,0,0,0), 0, 0
        )
    );
  }

  private JPanel getItemsPanel(String title, JList<ResourceIdentifier> list) {
    JPanel itemsPanel = new JPanel(new BorderLayout());
    itemsPanel.setBorder(
        BorderFactory.createTitledBorder(
            BorderFactory.createEmptyBorder(4,0,4,0),
            title)

    );
    itemsPanel.add(new JScrollPane(list), BorderLayout.CENTER);
    itemsPanel.setPreferredSize(new Dimension(240, 120));
    return itemsPanel;
  }

  private JPanel getButtonsPanel() {
    JPanel buttonsPanel = new JPanel(new GridBagLayout());

    //buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));

    JButton addAll = new JButton(">>");
    JButton addSelected = new JButton(">");
    JButton removeSelected = new JButton("<");
    JButton removeAll = new JButton("<<");

    GridBagConstraints gbc = new GridBagConstraints();

    buttonsPanel.add(addAll, gbc);
    gbc.gridy = 1;
    buttonsPanel.add(addSelected, gbc);
    gbc.gridy = 2;
    buttonsPanel.add(removeSelected, gbc);
    gbc.gridy = 3;
    buttonsPanel.add(removeAll, gbc);

    addAll.addActionListener(e -> addAllItemsToSelectedItemsList());
    addSelected.addActionListener(e -> addSelectedItemsToSelectedItemsList());
    removeSelected.addActionListener(e -> removeSelectedItemsFromSelectedItemsList());
    removeAll.addActionListener(e -> removeAllItemsFromSelectedItemsList());
    return buttonsPanel;
  }

  private void removeAllItemsFromSelectedItemsList() {
    ArrayList<ResourceIdentifier> models = Arrays.stream(selectedItemsModel.toArray())
        .map(i -> (ResourceIdentifier) i).collect(Collectors.toCollection(ArrayList::new));
    unSelectedItemsModel.addAll(models);
    selectedItemsModel.removeAllElements();
  }

  private void removeSelectedItemsFromSelectedItemsList() {
    Arrays.stream(rightList.getSelectedIndices())
        .mapToObj(selectedItemsModel::getElementAt)
        .collect(Collectors.toList())
        .forEach(i -> {
          selectedItemsModel.removeElement(i);
          unSelectedItemsModel.addElement(i);
        });
  }

  private void addSelectedItemsToSelectedItemsList() {
    Arrays.stream(leftList.getSelectedIndices())
        .mapToObj(unSelectedItemsModel::getElementAt)
        .collect(Collectors.toList())
        .forEach(i -> {
      unSelectedItemsModel.removeElement(i);
      selectedItemsModel.addElement(i);
    });
  }

  private void addAllItemsToSelectedItemsList() {
    ArrayList<ResourceIdentifier> models = Arrays.stream(unSelectedItemsModel.toArray())
        .map(i -> (ResourceIdentifier) i).collect(Collectors.toCollection(ArrayList::new));
    selectedItemsModel.addAll(models);
    unSelectedItemsModel.removeAllElements();
  }

  public List<ResourceIdentifier> getUnSelectedItems() {
    return Arrays.stream(unSelectedItemsModel.toArray())
        .map(i -> (ResourceIdentifier) i).collect(Collectors.toList());
  }

  public List<ResourceIdentifier> getSelectedItems() {
    return Arrays.stream(selectedItemsModel.toArray())
        .map(i -> (ResourceIdentifier) i).collect(Collectors.toList());
  }
}
