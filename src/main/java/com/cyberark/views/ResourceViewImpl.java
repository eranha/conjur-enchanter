package com.cyberark.views;

import com.cyberark.Util;
import com.cyberark.actions.*;
import com.cyberark.actions.resource.*;
import com.cyberark.components.DataTable;
import com.cyberark.components.RoleTableCellRenderer;
import com.cyberark.components.TitlePanel;
import com.cyberark.models.*;
import com.cyberark.models.table.AnnotationsTableModel;
import com.cyberark.models.table.DefaultResourceTableModel;
import com.cyberark.models.table.PermissionsTableModel;
import com.cyberark.models.table.ResourceTableModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.cyberark.Consts.ACTION_TYPE_KEY;
import static com.cyberark.Consts.CYBR_BLUE;
import static com.cyberark.util.Resources.getString;
import static javax.swing.event.TableModelEvent.DELETE;

/**
 * Base implementation of ResourceView interface.
 * @param <T> The ResourceModel of this ResourceView
 */
public class ResourceViewImpl<T extends ResourceModel> extends TitlePanel implements ResourceView {
  private static final Logger logger = LogManager.getLogger(ResourceViewImpl.class);

  private ResourceTableModel<T> resourceTableModel;
  private final ViewType view;
  private final DataTable resourceTable = new DataTable();
  private final JTable permissionsTable = new DataTable();
  private final JTable annotationsTable = new DataTable();
  private Consumer<DataModel> selectionListener;
  private Consumer<ResourceView> resourceDoubleClickedListener;
  private final ActionMap menuItemActions = new ActionMap();

  private final NewResourceActionFactory newResourceActionFactory = new NewResourceActionFactory();

  public ResourceViewImpl(ViewType view) {
    super(view.toString(), CYBR_BLUE);
    this.view = view;
    initializeComponents();
  }

  protected void initializeComponents() {
    JSplitPane splitPane = new JSplitPane(SwingConstants.HORIZONTAL);

    splitPane.setTopComponent(new JScrollPane(resourceTable));
    splitPane.setBottomComponent(getInfoPanel());

    setContent(splitPane);
    resourceTable.getSelectionModel().addListSelectionListener(this::onResourceSelectedEvent);
    getResourceTable().addMouseListener(getResourceTableMouseListener());
    registerActions();
    setResourceTablePopupMenu();
    splitPane.setDividerLocation(300);

    InputMap inputMap = resourceTable.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    ActionMap actionMap = resourceTable.getActionMap();

    if (getAction(ActionType.DeleteItem) != null) {
      inputMap.put((KeyStroke)getAction(ActionType.DeleteItem).getValue(Action.ACCELERATOR_KEY), DELETE);
      actionMap.put(DELETE, getAction(ActionType.DeleteItem));
    }
  }

  /**
   * Returns Permissions and Annotations tables
   * @return Permissions and Annotations tables
   */
  protected Component getInfoPanel() {
    JSplitPane bottomSplitPane = new JSplitPane(SwingConstants.VERTICAL);
    TitlePanel permissionsPanel = new TitlePanel(
        getString("resource.view.permissions"),
        new JScrollPane(permissionsTable),
        CYBR_BLUE);
    TitlePanel annotationsPanel = new TitlePanel(
        getString("resource.view.annotations"),
        new JScrollPane(annotationsTable),
        CYBR_BLUE);

    permissionsTable.setDefaultRenderer(ResourceIdentifier.class, new RoleTableCellRenderer());
    bottomSplitPane.setLeftComponent(permissionsPanel);
    bottomSplitPane.setRightComponent(annotationsPanel);
    bottomSplitPane.setDividerLocation(360);

    return bottomSplitPane;
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
    if (event.getValueIsAdjusting()) {
      return;
    }

    if (resourceTable.getModel() instanceof ResourceTableModel) {
      if (getResourceTable().getSelectedRow() > -1) {
        T model = resourceTableModel.getResourceModel(getResourceTable().getSelectedRow());

        if (selectionListener != null) {
          selectionListener.accept(model);
        }

        populateResourceData(model);
      }
    }
  }

  @Override
  public ActionMap getResourceActions() {
    return menuItemActions;
  }

  /**
   * Constructs this view resource table popup menu based on the DEFAULT_MENU_ITEMS_ORDER
   * adding first the items in DEFAULT_MENU_ITEMS_ORDER if applicable.
   * @return JPopupMenu
   */
  private JPopupMenu constructComponentPopupMenu() {
    JPopupMenu popupMenu = new JPopupMenu();

    Arrays.stream(ActionType.values())
    .forEach(type -> {
        Action action = menuItemActions.get(type);

        if (action == null) {
          logger.debug(
              "ResourceViewImpl{}::constructComponentPopupMenu there is no action for action type: {}",
              view, type
          );

          return;
        }

        if (menuItemActions.size() > 2 && type == ActionType.DeleteItem) {
          popupMenu.addSeparator();
        }

        popupMenu.add(new JMenuItem(action));

        if (menuItemActions.size() > 3 && type == ActionType.DeleteItem) {
          popupMenu.addSeparator();
        }
      }
    );

    return popupMenu;
  }

  protected void populateResourceData(T resourceModel) {
    permissionsTable.setModel(new PermissionsTableModel(
        resourceModel != null
            ? resourceModel.getPermissions()
            : new Permission[0]));
    annotationsTable.setModel(new AnnotationsTableModel(
        resourceModel != null
            ? resourceModel.getAnnotations()
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
      logger.warn("WARNING! Action: {} has no {} property and will be ignored", ACTION_TYPE_KEY, action);
      return;
    }
    actionMap.put(action.getValue(ACTION_TYPE_KEY), action);
  }

  protected List<Action> getMenuActions() {
    List<Action> actions = new ArrayList<>();
    Action newResourceAction = getNewResourceTypeAction();

    if (Objects.nonNull(newResourceAction)) {
      newResourceAction.putValue(Action.NAME,
          getString("resource.view.new.resource.action.name"));
      actions.add(
          newResourceAction
      );
    }

    if (Util.isSetResource(Util.getResourceType(view))) {
      actions.add(new EditSetResourceAction(this::getSelectedResource,
          getString("edit.set.resource.action.text")));
    }

    // general actions for all resource types
    actions.add(new EditAnnotationsAction<>(this::getSelectedResource));
    actions.add(new DeleteItemAction<>(this::getSelectedResource,
        getString("resource.view.delete.resource.action.name")));
    actions.add(new DuplicateItemAction<>(this::getSelectedResource));
    actions.add(new EditPermissions<>(this::getSelectedResource));
    actions.add(new ViewResourcePolicyAction<>(this::getSelectedResource));

    return actions;
  }

  /**
   * Adds the default action any ResourceView supports.
   * Classes that extends ResourceView can override this method to add their own specific actions.
   * @return list of actions
   */
  protected List<Action> getActions() {
    List<Action> actions = new ArrayList<>();
    Action newResource = getNewResourceTypeAction();

    if (Objects.nonNull(newResource)) {
      actions.add(getNewResourceTypeAction());
    }

    if (Util.isSetResource(Util.getResourceType(view))) {
      actions.add(new EditSetResourceAction(this::getSelectedResource));
    }

    actions.add(new DeleteItemAction<>(this::getSelectedResource));

    return actions;
  }

  private Action getNewResourceTypeAction() {
    return newResourceActionFactory.getAction(getResourceType());
  }

  /**
   * Notifies when a resource is selected in the resource table.
   * Classes that extends ResourceView can override this method to get notified.
   */
  protected void mouseDoubleClickOnResource() {
    if (resourceDoubleClickedListener != null) {
      resourceDoubleClickedListener.accept(this);
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
        .filter(r -> r.getId().contains(query))
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

  @Override
  public void setModel(ViewModel model) {
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
  public void setTableRowDoubleClickedEventListener(Consumer<ResourceView> consumer) {
    this.resourceDoubleClickedListener = consumer;
  }

  public void setResourceTableModel(ResourceTableModel<T> model) {
    T selectedResource = getSelectedResource();
    resourceTableModel = model;
    getResourceTable().setModel(resourceTableModel);

    if (selectedResource != null) {
      restoreSelection(selectedResource);
    }
  }
}
