package com.cyberark.views;

import com.cyberark.Util;
import com.cyberark.actions.*;
import com.cyberark.components.DataTable;
import com.cyberark.components.TitlePanel;
import com.cyberark.models.*;
import com.cyberark.models.table.AnnotationsTableModel;
import com.cyberark.models.table.DefaultResourceTableModel;
import com.cyberark.models.table.PermissionsTableModel;
import com.cyberark.models.table.ResourceTableModel;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.cyberark.Consts.ACTION_TYPE_KEY;
import static com.cyberark.Consts.CYBR_BLUE;
import static javax.swing.event.TableModelEvent.DELETE;

/**
 * Base implementation of ResourceView interface.
 * @param <T> The ResourceModel of this ResourceView
 */
public class ResourceViewImpl<T extends ResourceModel> extends TitlePanel implements ResourceView {
  private ResourceTableModel<T> resourceTableModel;
  private final ViewType view;
  private final DataTable resourceTable = new DataTable();
  private final JTable permissionsTable = new DataTable();
  private final JTable annotationsTable = new DataTable();
  private Consumer<DataModel> selectionListener;
  private Consumer<DataModel> resourceDoubleClickedListener;
  private final ActionMap menuItemActions = new ActionMap();
  private final static ActionType[] DEFAULT_MENU_ITEMS_ORDER = new ActionType[] {
      ActionType.NewItem,
      ActionType.EditItem,
      ActionType.DeleteItem,
      ActionType.ViewPolicy,
      ActionType.DuplicateItem
  };

  private final NewResourceActions newResourceActions = new NewResourceActions();

  public ResourceViewImpl(ViewType view) {
    super(view.toString(), CYBR_BLUE);
    this.view = view;
    initializeComponents();
  }

  protected void initializeComponents() {
    JSplitPane splitPane = new JSplitPane(SwingConstants.HORIZONTAL);
    JSplitPane bottomSplitPane = new JSplitPane(SwingConstants.VERTICAL);

    TitlePanel permissionsPanel = new TitlePanel("Permissions", new JScrollPane(permissionsTable), CYBR_BLUE);
    TitlePanel annotationsPanel = new TitlePanel("Annotations", new JScrollPane(annotationsTable), CYBR_BLUE);
    bottomSplitPane.setLeftComponent(permissionsPanel);
    bottomSplitPane.setRightComponent(annotationsPanel);

    splitPane.setTopComponent(new JScrollPane(resourceTable));
    splitPane.setBottomComponent(bottomSplitPane);
    setContent(splitPane);
    resourceTable.getSelectionModel().addListSelectionListener(this::onResourceSelectedEvent);
    getResourceTable().addMouseListener(getResourceTableMouseListener());
    registerActions();
    setResourceTablePopupMenu();
    splitPane.setDividerLocation(300);
    bottomSplitPane.setDividerLocation(360);

    InputMap inputMap = resourceTable.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    ActionMap actionMap = resourceTable.getActionMap();

    if (getAction(ActionType.DeleteItem) != null) {
      inputMap.put((KeyStroke)getAction(ActionType.DeleteItem).getValue(Action.ACCELERATOR_KEY), DELETE);
      actionMap.put(DELETE, getAction(ActionType.DeleteItem));
    }
  }

  private void setResourceTablePopupMenu() {
    if (getActionMap().size() > 0) {
      JPopupMenu popupMenu = constructComponentPopupMenu();
      getResourceTable().setComponentPopupMenu(popupMenu);
    }
  }

  private MouseAdapter getResourceTableMouseListener() {
    return new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        super.mousePressed(e);

        Point point = e.getPoint();
        int currentRow = getResourceTable().rowAtPoint(point);
        resourceTable.setRowSelectionInterval(currentRow, currentRow);
        boolean isResourceDoubleClicked = e.getClickCount() == 2 &&
            getResourceTable().getSelectedRow() != -1;

        if (isResourceDoubleClicked) {
          mouseDoubleClickOnResource();
        }
      }
    };
  }

  private void onResourceSelectedEvent(ListSelectionEvent event) {
    if (event.getValueIsAdjusting()) return;
    if (event.getSource() instanceof ListSelectionModel) {
      ListSelectionModel lsm = (ListSelectionModel) event.getSource();

      if (resourceTable.getModel() instanceof ResourceTableModel) {
        var model = (ResourceTableModel<? extends ResourceModel>) resourceTable.getModel();
        ResourceModel resourceModel = lsm.isSelectionEmpty()
            ? null
            : model.getResourceModel(lsm.getMinSelectionIndex());

        if (selectionListener != null) {
          selectionListener.accept(resourceModel);
        }

        populateResourceData(resourceModel);
      }
    }
  }

  /**
   * Constructs this view resource table popup menu based on the DEFAULT_MENU_ITEMS_ORDER
   * adding first the items in DEFAULT_MENU_ITEMS_ORDER if applicable.
   * @return JPopupMenu
   */
  private JPopupMenu constructComponentPopupMenu() {
    JPopupMenu popupMenu = new JPopupMenu();

    for (ActionType type : DEFAULT_MENU_ITEMS_ORDER) {
      Action action = menuItemActions.get(type);

      if (menuItemActions.size() > 2 && type == ActionType.DeleteItem) {
        popupMenu.addSeparator();
      }

      if (menuItemActions.get(type) != null) {
        popupMenu.add(new JMenuItem(action));
      }

      if (menuItemActions.size() > 3 && type == ActionType.DeleteItem) {
        popupMenu.addSeparator();
      }
    }

    Set<ActionType> set = Arrays.stream(DEFAULT_MENU_ITEMS_ORDER).collect(Collectors.toSet());

    // Add the rest of items not in order
    Arrays.stream(menuItemActions.keys())
        .filter(i -> i instanceof ActionType)
        .filter(i -> !set.contains((ActionType)i))
        .forEach(i -> popupMenu.add(new JMenuItem(menuItemActions.get(i))));

    return popupMenu;
  }

  private void populateResourceData(ResourceModel resourceModel) {
    permissionsTable.setModel(new PermissionsTableModel(
        resourceModel != null
            ? resourceModel.permissions
            : new Permission[0]));
    annotationsTable.setModel(new AnnotationsTableModel(
        resourceModel != null
            ? resourceModel.annotations
            : new Annotation[0]));
  }

  /**
   * Map this resource view actions to their ActionType property.
   * Action with no 'action.type' property are ignored.
   */
  private void registerActions() {
    getActions().forEach(a -> {
      if (a.getValue(ACTION_TYPE_KEY) instanceof ActionType) {
        registerAction(getActionMap(), a);
      }
    });

    List<Action> menuActions = getMenuActions();
    menuActions.forEach(i -> registerAction(menuItemActions, i));
  }

  private void registerAction(ActionMap actionMap, Action action) {
    if (action.getValue(ACTION_TYPE_KEY) == null) {
      System.out.printf("WARNING! Action: %s has so no %s property and will be ignored.%n", ACTION_TYPE_KEY, action);
      return;
    }
    actionMap.put(action.getValue(ACTION_TYPE_KEY), action);
  }

  protected List<Action> getMenuActions() {

    List<Action> actions = new ArrayList<>();
    Action action = getNewResourceTypeAction();
    action.putValue(Action.NAME, "New...");
    actions.add(
        action
    );

    if (Util.isSetResource(Util.getResourceType(view))) {
      actions.add(new EditSetResourceAction(this::getSelectedResource, "Edit Members..."));
    }

    actions.add(new DeleteItemAction<>(this::getSelectedResource, "Delete..."));
    actions.add(new DuplicateItemAction<>(this::getSelectedResource));
    actions.add(new EditPermissions<>(this::getSelectedResource));
    actions.add(new ViewResourcePolicyAction<>(this::getSelectedResource));
    actions.add(new EditPermissions<>(this::getSelectedResource));

    return actions;
  }

  /**
   * Adds the default action any ResourceView supports.
   * Classes that extends ResourceView can override this method to add their own specific actions.
   * @return list of actions
   */
  protected List<Action> getActions() {
    List<Action> actions = new ArrayList<>();

    actions.add(getNewResourceTypeAction());

    if (Util.isSetResource(Util.getResourceType(view))) {
      actions.add(new EditSetResourceAction(this::getSelectedResource));
    }

    actions.add(new DeleteItemAction<>(this::getSelectedResource));

    return actions;
  }

  private Action getNewResourceTypeAction() {
    return newResourceActions.getAction(getResourceType());
  }

  /**
   * Notifies when a resource is selected in the resource table.
   * Classes that extends ResourceView can override this method to get notified.
   */
  protected void mouseDoubleClickOnResource() {
    if (resourceDoubleClickedListener != null) {
      resourceDoubleClickedListener.accept(getSelectedResource());
    }
  }

  @Override
  public void toggleSelectionBasedActions(boolean enabled) {
    Arrays.stream(getActionMap().keys()).forEach(k -> toggleSelectionBasedAction(getActionMap().get(k), enabled));
    Arrays.stream(menuItemActions.keys()).forEach(k -> toggleSelectionBasedAction(menuItemActions.get(k), enabled));
  }

  private void toggleSelectionBasedAction(Action action, boolean enabled) {
    if (action instanceof ResourceAction && ((ResourceAction)action).isSelectionBased()) {
      action.setEnabled(enabled);
    }
  }

  @Override
  public void applyFilter(String query) {
    resourceTable.setModel(resourceTableModel);

    List<T> items = resourceTableModel.getResourceModels()
        .stream()
        .filter(r -> r.id.contains(query))
        .collect(Collectors.toList());

    resourceTable.setModel(createTableModel(items));
  }

  protected ResourceTableModel<T> createTableModel(List<T> items) {
    return new DefaultResourceTableModel<>(items);
  }

  @Override
  public ViewType getType() {
    return view;
  }

  @Override
  public Component getComponent() {
    return this;
  }

  public JTable getResourceTable() {
    return resourceTable;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void setModel(ViewModel model) {
    if (!(model instanceof ResourceTableModel)) {
      throw new IllegalArgumentException("model");
    }

    T selectedResource = getSelectedResource();
    resourceTableModel = (ResourceTableModel<T>) model;
    getResourceTable().setModel(resourceTableModel);

    if (selectedResource != null) {
      restoreSelection(selectedResource);
    }
  }

  private void restoreSelection(T selectedResource) {
    int index = resourceTableModel.getResourceModelIndex(selectedResource);

    if (index > -1) {
      getResourceTable().setRowSelectionInterval(index, index);
    }
  }

  @Override
  public void clearData() {
    getModel().clearData();
  }

  protected ResourceTableModel<T> getModel() {
    return resourceTableModel;
  }

  protected T  getSelectedResource() {
    if (isTableRowSelected()) {
      return resourceTableModel.getResourceModel(getResourceTable().getSelectedRow());
    }

    return null;
  }

  private boolean isTableRowSelected() {
    return getResourceTable().getSelectedRow() > -1;
  }

  @Override
  public Action getAction(ActionType actionType) {
    return getActionMap().get(actionType);
  }

  private ResourceType getResourceType() {
    return Util.getResourceType(view);
  }

  @Override
  public void setSelectionListener(Consumer<DataModel> consumer) {
    this.selectionListener = consumer;
  }

  @Override
  public void setTableRowDoubleClickedEventListener(Consumer<DataModel> consumer) {
    this.resourceDoubleClickedListener = consumer;
  }
}
