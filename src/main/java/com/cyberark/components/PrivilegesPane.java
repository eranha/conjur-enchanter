package com.cyberark.components;

import com.cyberark.dialogs.InputDialog;
import com.cyberark.models.Permission;
import com.cyberark.models.Privilege;
import com.cyberark.models.ResourceIdentifier;
import com.cyberark.models.ResourceType;
import com.cyberark.models.table.PrivilegesTableModel;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class PrivilegesPane extends ContainerBase {
  private static final PrivilegesTableModel PRIVILEGES_EMPTY_TABLE_MODEL = new PrivilegesTableModel(null, new HashMap<>());
  private final List<ResourceIdentifier> resourceModels;
  private final Map<ResourceIdentifier, Set<String>> resourcePrivileges = new HashMap<>();
  private final Map<ResourceIdentifier, PrivilegesTableModel> privilegesTableModels = new HashMap<>();
  private final ResourceType resourceType;
  private EditableTableImpl<Privilege> privilegesTable;

  public PrivilegesPane(String resourcesType,
                        ResourceType resourceType,
                        Permission[] permissions,
                        List<ResourceIdentifier> roles) {
    this.resourceModels = roles;
    this.resourceType = resourceType;

    Arrays.stream(permissions).forEach(p -> {
      resourcePrivileges.computeIfAbsent(ResourceIdentifier.fromString(p.getRole()), v -> new HashSet<>());
      resourcePrivileges.get(ResourceIdentifier.fromString(p.getRole())).add(p.getPrivilege());
    });

    initializeComponents(resourcesType);
  }

  public Map<ResourceIdentifier, Set<String>> getPrivileges() {
    return resourcePrivileges;
  }

  private void initializeComponents(String resourcesType) {
    JList<ResourceIdentifier> rolesList = new JList<>();
    DefaultListModel<ResourceIdentifier> rolesListModel = new DefaultListModel<>();
    JLabel label = new JLabel(String.format("%s:", resourcesType));
    JButton addRoleButton = new JButton(getString("privileges.pane.add.label.text"));
    JButton removeRoleButton = new JButton(getString("privileges.pane.remove.label.text"));

    JPanel topPanel = new JPanel(new BorderLayout());
    JPanel listPanel = new JPanel(new BorderLayout());

    removeRoleButton.setEnabled(false);
    rolesList.addListSelectionListener(e -> removeRoleButton.setEnabled(rolesList.getSelectedIndices().length > 0));

    removeRoleButton.addActionListener(e -> {
          ResourceIdentifier role =  rolesList.getSelectedValue();
          rolesListModel.removeElement(role);
          resourcePrivileges.get(role).clear();
        }
    );

    resourcePrivileges.keySet().forEach(rolesListModel::addElement);

    addRoleButton.addActionListener(e -> handleAddRoleEvent(rolesList, rolesListModel));

    rolesListModel.addListDataListener(getRoleListDataListener(rolesListModel));

    privilegesTable = new EditableTableImpl<>(
        new PrivilegesTableModel(
            (rolesListModel.getSize() > 0)
              ? rolesListModel.get(0).getId()
              : null,
            PrivilegesTableModel.EXECUTE_PRIVILEGES),
        m -> new Privilege(String.format(
            getString("privileges.pane.new.item.text"), m.getRowCount() + 1), false));

    rolesList.setCellRenderer(new ResourceListItemCellRenderer());
    rolesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    rolesList.addListSelectionListener(e -> handleRolesListSelectionEvent(rolesList, rolesListModel, e));

    topPanel.add(label, BorderLayout.NORTH);
    listPanel.add(new JScrollPane(rolesList), BorderLayout.CENTER);
    listPanel.setBorder(BorderFactory.createEmptyBorder(6,0,2,6));
    topPanel.add(listPanel, BorderLayout.CENTER);
    rolesList.setModel(rolesListModel);
    listPanel.setPreferredSize(new Dimension(320, 240));

    JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
    bottomPanel.add(addRoleButton);
    bottomPanel.add(removeRoleButton);
    topPanel.add(bottomPanel, BorderLayout.SOUTH);
    setLayout(new BorderLayout());
    topPanel.setBorder(BorderFactory.createEmptyBorder(0,0,8,0));
    add(topPanel, BorderLayout.CENTER);


    privilegesTable.getTable().getTableHeader().setDefaultRenderer(new HeaderRenderer(privilegesTable.getTable()));
    privilegesTable.setPreferredSize(new Dimension(360, 240));
    privilegesTable.setBorder(BorderFactory.createEmptyBorder(0,0,0,4));
    add(privilegesTable, BorderLayout.SOUTH);

    if (!resourcePrivileges.isEmpty()) {
      rolesList.setSelectedIndex(0);
    } else  {
      privilegesTable.getTable().setModel(PRIVILEGES_EMPTY_TABLE_MODEL);
    }
  }

  private ListDataListener getRoleListDataListener(DefaultListModel<ResourceIdentifier> rolesListModel) {
    return new ListDataListener() {
      @Override
      public void intervalAdded(ListDataEvent e) {
        resourcePrivileges.put(rolesListModel.elementAt(e.getIndex0()), new HashSet<>());
      }

      @Override
      public void intervalRemoved(ListDataEvent e) {
      }

      @Override
      public void contentsChanged(ListDataEvent e) {
      }
    };
  }

  private void handleRolesListSelectionEvent(JList<ResourceIdentifier> rolesList,
                                             DefaultListModel<ResourceIdentifier> rolesListModel,
                                             ListSelectionEvent e) {
    if(e.getValueIsAdjusting()) {
      return;
    }

    ResourceIdentifier resource = rolesList.getSelectedValue();

    if (rolesListModel.isEmpty() || resource == null) {
      // set empty model
      privilegesTable.getTable().setModel(PRIVILEGES_EMPTY_TABLE_MODEL);
      return;
    }

    privilegesTableModels.computeIfAbsent(
        resource,
        v -> createPrivilegesTableModel(rolesList, rolesListModel, resource)
    );

    PrivilegesTableModel model = privilegesTableModels.get(resource);
    privilegesTable.getTable().setModel(model);
    model.fireTableDataChanged();
  }

  private PrivilegesTableModel createPrivilegesTableModel(JList<ResourceIdentifier> rolesList,
                                                          DefaultListModel<ResourceIdentifier> rolesListModel,
                                                          ResourceIdentifier resource) {

    Map<String, Boolean> map = new HashMap<>(
        resourceType == ResourceType.variable || resourceType == ResourceType.webservice
        ? PrivilegesTableModel.EXECUTE_PRIVILEGES
        : PrivilegesTableModel.CREATE_UPDATE_PRIVILEGES
    );

    resourcePrivileges.get(resource).forEach(
        p -> map.put(p, true)
    );

    PrivilegesTableModel model = new PrivilegesTableModel(resource.getId(), map);

    model.addTableModelListener(me -> {
      // upon any change in table reconstruct the privileges list
      Set<String> privileges = resourcePrivileges.get(
          rolesListModel.getElementAt(
              rolesList.getSelectedIndices()[0]));
      privileges.clear();

      ((PrivilegesTableModel)me.getSource()).getPrivileges()
        .stream()
        .filter(Privilege::isAllow)
        .forEach(p -> privileges.add(p.getPrivilege()));
    });

    return model;
  }

  private void handleAddRoleEvent(JList<ResourceIdentifier> permittedRoles,
                                  DefaultListModel<ResourceIdentifier> permittedRolesModel) {
    List<ResourceIdentifier> identifiers =
        resourceModels
            .stream()
            .filter(i -> !permittedRolesModel.contains(i))
            .collect(Collectors.toList());

    JList<ResourceIdentifier> list = new JList<>();
    DefaultListModel<ResourceIdentifier> rolesModel = new DefaultListModel<>();
    identifiers.forEach(rolesModel::addElement);

    list.setModel(rolesModel);
    list.setCellRenderer(new ResourceListItemCellRenderer());
    list.setPreferredSize(new Dimension(320, 240));
    list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    list.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent evt) {
        if (evt.getClickCount() == 2) {
          int index = list.locationToIndex(evt.getPoint());

          if (index > -1) {
            Window ancestor = SwingUtilities.getWindowAncestor(list);

            if (ancestor instanceof JDialog
                && ((JDialog)ancestor).getRootPane().getDefaultButton() != null) {
              ((JDialog)ancestor).getRootPane().getDefaultButton().doClick();
            }
          }
        }
      }
    });

    if (InputDialog.showModalDialog(SwingUtilities.getWindowAncestor(this),
        getString("privileges.pane.select.roles.dialog.title"),
        new JScrollPane(list)
    ) == InputDialog.OK_OPTION) {
      // add the selected roles to the permitted roles list
      Arrays.stream(list.getSelectedIndices())
          .forEach(i -> permittedRolesModel.addElement(rolesModel.getElementAt(i)));
      // select the first of the selected roles
      if (list.getSelectedIndices().length > 0) {
        permittedRoles.setSelectedValue(
            rolesModel.getElementAt(list.getSelectedIndices()[0]),
            true);
      }
    }
  }

  public Map<ResourceIdentifier, Set<String>> getResourcePrivileges() {
    return resourcePrivileges;
  }

  // Table Cell Renderer
  private static class HeaderRenderer implements TableCellRenderer {

    DefaultTableCellRenderer renderer;

    public HeaderRenderer(JTable table) {
      renderer = (DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer();
    }

    @Override
    public Component getTableCellRendererComponent(
        JTable table, Object value, boolean isSelected,
        boolean hasFocus, int row, int col) {
      renderer.setHorizontalAlignment(col > 0 ? JLabel.CENTER : JLabel.LEFT);

      return renderer.getTableCellRendererComponent(
          table,
          value,
          isSelected,
          hasFocus,
          row,
          col
      );
    }
  }
}
