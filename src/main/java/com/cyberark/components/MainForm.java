package com.cyberark.components;

import com.cyberark.Application;
import com.cyberark.Util;
import com.cyberark.actions.*;
import com.cyberark.actions.resource.NewResourceActionFactory;
import com.cyberark.event.Events;
import com.cyberark.event.ViewSelectedListener;
import com.cyberark.models.ResourceModel;
import com.cyberark.models.ResourceType;
import com.cyberark.views.Icons;
import com.cyberark.views.ResourceView;
import com.cyberark.views.View;
import com.cyberark.views.ViewType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.cyberark.Consts.*;
import static com.cyberark.util.Resources.getString;

public class MainForm extends JFrame {
  private final JTextField searchTextField = new JTextField();
  private final Map<ViewType, NavigationLabel> navigationsLabels = new HashMap<>();
  private final ActionListener actionListener;
  private ViewSelectedListener viewSelectedListener;
  private View view;
  private final JPanel mainView = new JPanel(new BorderLayout());
  private final JLabel statusLabel = new JLabel(" ");
  private final AbstractButton editButton = createToolBarButton(getString("main.form.toolbar.edit"));
  private final AbstractButton deleteButton = createToolBarButton(getString("main.form.toolbar.delete"));
  private final ToolBarLabel userLabelText = new ToolBarLabel("",
      Icons.getInstance().getIcon(ResourceType.user, 16, CYBR_BLUE));
  private NavigationLabel selectedNavigationLabel;
  private static final Logger logger = LogManager.getLogger(MainForm.class);
  private final JPanel contentPane = new JPanel(new BorderLayout());
  private final JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
  private final JScrollPane scrollPane = new JScrollPane();
  private final JMenu resourceMenu = new JMenu(getString("main.form.resource.menu"));

  public MainForm(ViewSelectedListener viewSelectedListener, ActionListener actionListener) {
    this.viewSelectedListener = viewSelectedListener;
    this.actionListener = actionListener;
    initializeComponents();
  }

  @Override
  public void setVisible(boolean b) {
    super.setVisible(b);
    String user = b ? Application.getInstance().getUser() : null;
    logger.debug("Set user label text to user: {}", user);
    userLabelText.setText(user);
  }

  public void setStatusLabel(String msg) {
    logger.debug("Set status label text: {}", msg);
    statusLabel.setText(msg);
    new Timer(5000, e -> statusLabel.setText(" ")).start();
  }

  public void setViewSelectedListener(ViewSelectedListener viewSelectedListener) {
    this.viewSelectedListener = viewSelectedListener;
  }

  public void toggleLogView(boolean show) {
    if (show) {
      getContentPane().remove(contentPane);
      scrollPane.setViewportView(contentPane);
      splitPane.setTopComponent(scrollPane);
      splitPane.setBottomComponent(ApiCallLogView.getInstance());
      splitPane.setDividerLocation(getSize().height/2);
      getContentPane().add(splitPane, BorderLayout.CENTER);
      ApiCallLogView.getInstance().scrollToEnd();
    } else {
      getContentPane().remove(splitPane);
      scrollPane.setViewportView(null);
      getContentPane().add(contentPane, BorderLayout.CENTER);
    }
    revalidate();
  }

  private void initializeComponents() {
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    getContentPane().setLayout(new BorderLayout());
    contentPane.add(mainView, BorderLayout.CENTER);

    mainView.add(createToolbar(), BorderLayout.NORTH);
    setJMenuBar(creatMenuBar());

    JPanel leftPanel = new JPanel(new BorderLayout());
    leftPanel.setBackground(DARK_BG);

    JPanel buttonsPanelWrapper = new JPanel(new GridBagLayout());
    JPanel buttonsPanel = new JPanel();
    leftPanel.add(buttonsPanelWrapper, BorderLayout.CENTER);
    ImageIcon img = getLogoIcon();
    JLabel logo =  new JLabel(
        new ImageIcon(img.getImage().getScaledInstance(160, 24,  java.awt.Image.SCALE_SMOOTH)));
    logo.setBorder(BorderFactory.createEmptyBorder(16,0, 8, 8));

    leftPanel.add(logo, BorderLayout.NORTH);

    leftPanel.add(Box.createRigidArea(new Dimension(0, 1)), BorderLayout.LINE_END);
    buttonsPanelWrapper.setBackground(DARK_BG);
    buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
    buttonsPanel.setBackground(DARK_BG);

    // Build the navigation panel
    Arrays.stream(ViewType.values())
        .forEach(view -> buttonsPanel.add(createNavigationLabel(view)));

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.BOTH;
    gbc.weightx = 1;
    gbc.weighty = 1;
    buttonsPanelWrapper.add(buttonsPanel, gbc);

    // bottom spacer
    gbc.gridy = 1;
    gbc.weightx = 1;
    gbc.weighty = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTH;
    buttonsPanelWrapper.add(
        Box.createRigidArea(new Dimension(1, 80)),
        gbc);

    contentPane.add(leftPanel, BorderLayout.WEST);

    // status
    JPanel statusPanel = new JPanel(new BorderLayout());
    statusPanel.setBorder(BorderFactory.createEmptyBorder(4,4,4, 0));
    statusPanel.add(statusLabel, BorderLayout.WEST);
    contentPane.add(statusPanel, BorderLayout.SOUTH);


    setTitle(String.format("Conjur %s", getString("application.name")));
    setVisible(false);
    Dimension size
        = Toolkit.getDefaultToolkit().getScreenSize();
    setBounds(10, 10, size.width-size.width/4, size.height-size.height/4);

    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    getContentPane().add(contentPane, BorderLayout.CENTER);
  }

  private NavigationLabel createNavigationLabel(ViewType view) {
    NavigationLabel label = new NavigationLabel(view, this::handleNavigationEvent);
    navigationsLabels.put(view, label);

    return label;
  }

  private JMenuBar creatMenuBar() {
    NewResourceActionFactory actionMap = new NewResourceActionFactory();
    JMenuBar menuBar = new JMenuBar();

    JMenu fileMenu = new JMenu(getString("main.form.file.menu"));
    fileMenu.setMnemonic(KeyEvent.VK_F);
    menuBar.add(fileMenu);
    JMenu newMenu = new JMenu(getString("main.form.new.menu"));
    fileMenu.add(newMenu);

    // TODO Add HOST_FACTORY if applicable
    Arrays.stream(ResourceType.values())
        .map(actionMap::getAction)
        .filter(Objects::nonNull)
        .forEach(
          action -> newMenu.add(new JMenuItem(action)
        )
    );

    JMenuItem exit = new JMenuItem(getString("main.form.exit.menu.item"));
    exit.addActionListener(e -> System.exit(0));
    fileMenu.add(exit);


    menuBar.add(resourceMenu);
    resourceMenu.setEnabled(false);


    JMenu viewMenu = new JMenu(getString("main.form.view.menu"));
    viewMenu.add(new ViewApiCallLog(this::toggleLogView));
    menuBar.add(viewMenu);

    JMenu helpMenu = new JMenu(getString("main.form.help.menu"));
    menuBar.add(helpMenu);

    JMenuItem conjurHomeMenuItem = new JMenuItem(getString("main.form.help.menu.conjur.item"));
    conjurHomeMenuItem.addActionListener(e -> actionListener.actionPerformed(
        new ActionEvent(conjurHomeMenuItem, Events.HELP, "conjur.home.uri")
    ));
    helpMenu.add(conjurHomeMenuItem);

    JMenuItem policySyntaxMenuItem = new JMenuItem(getString("main.form.help.menu.policy.syntax.item"));
    policySyntaxMenuItem.addActionListener(e -> actionListener.actionPerformed(
        new ActionEvent(conjurHomeMenuItem, Events.HELP, "policy.syntax.uri")
    ));
    helpMenu.add(policySyntaxMenuItem);

    JMenuItem getStartedMenuItem = new JMenuItem(getString("main.form.help.menu.getting.started.item"));
    getStartedMenuItem.addActionListener(e -> actionListener.actionPerformed(
        new ActionEvent(conjurHomeMenuItem, Events.HELP, "get.started.uri")
    ));
    helpMenu.add(getStartedMenuItem);


    JMenu policyStatementReferenceHelpMenu = new JMenu(getString("main.form.help.menu.policy.statement.item"));
    Arrays.stream(ResourceType.values()).forEach(
        i -> {
          JMenuItem item = new JMenuItem(Util.resourceTypeToTitle(i));
          item.addActionListener(e -> actionListener.actionPerformed(
              new ActionEvent(conjurHomeMenuItem, Events.HELP, String.format("%s.help.uri", i))
          ));
          policyStatementReferenceHelpMenu.add(item);
        }
    );

    JMenuItem permitMenuItem = new JMenuItem(
        getString("main.form.help.menu.policy.statement.permit.item")
    );
    permitMenuItem.addActionListener(e -> actionListener.actionPerformed(
        new ActionEvent(conjurHomeMenuItem, Events.HELP, "permit.uri")
    ));
    policyStatementReferenceHelpMenu.add(permitMenuItem);

    JMenuItem denyMenuItem = new JMenuItem(
        getString("main.form.help.menu.policy.statement.deny.item")
    );
    denyMenuItem.addActionListener(e -> actionListener.actionPerformed(
        new ActionEvent(conjurHomeMenuItem, Events.HELP, "deny.uri")
    ));
    policyStatementReferenceHelpMenu.add(denyMenuItem);

    JMenuItem grantMenuItem = new JMenuItem(
        getString("main.form.help.menu.policy.statement.grant.item")
    );
    grantMenuItem.addActionListener(e -> actionListener.actionPerformed(
        new ActionEvent(conjurHomeMenuItem, Events.HELP, "grant.uri")
    ));
    policyStatementReferenceHelpMenu.add(grantMenuItem);

    JMenuItem revokeMenuItem = new JMenuItem(
        getString("main.form.help.menu.policy.statement.revoke.item")
    );
    revokeMenuItem.addActionListener(e -> actionListener.actionPerformed(
        new ActionEvent(conjurHomeMenuItem, Events.HELP, "revoke.uri")
    ));
    policyStatementReferenceHelpMenu.add(revokeMenuItem);

    helpMenu.add(policyStatementReferenceHelpMenu);


    return menuBar;
  }

  private JToolBar createToolbar() {
    UIManager.put("ToolBar.background", DARK_BG);
    UIManager.put("ToolBar.foreground", Color.WHITE);
    JToolBar toolBar = new JToolBar();
    toolBar.setFloatable( false);
    toolBar.setRollover(true);
    UIManager.put("PopupMenu.consumeEventOnClose", Boolean.TRUE);
    toolBar.add(createNewItemButton());
    toolBar.add(editButton);
    toolBar.add(deleteButton);
    toolBar.add(createToolBarButton(new ReloadViewAction()));
    toolBar.add(createToolBarButton(new ShowResourceTreeAction()));
    toolBar.add(Box.createRigidArea(new Dimension(8,1)));
    toolBar.add(searchTextField);
    searchTextField.getDocument().addDocumentListener(new DefaultDocumentListener(this::search));
    toolBar.add(Box.createRigidArea(new Dimension(8,1)));
    toolBar.add(new JLabel(Icons.getInstance().getIcon(Icons.SEARCH_ICON_UNICODE, 16, CYBR_BLUE)));
    toolBar.add(Box.createRigidArea(new Dimension(16,1)));
    toolBar.add(Box.createHorizontalGlue());
    toolBar.add(userLabelText);
    toolBar.add(Box.createRigidArea(new Dimension(16,1)));
    toolBar.add(createToolBarButton(new LogoutAction()));

    return toolBar;
  }

  private AbstractButton createNewItemButton() {
    NewResourceActionFactory actionMap = new NewResourceActionFactory();
    final JToggleButton button = (JToggleButton) new ToolBarButton(new JToggleButton(
        getString("main.form.toolbar.new"))).getButton();
    button.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        createAndShowNewItemsMenu(actionMap, (JComponent) e.getSource(), button);
      }
    });
    button.setFocusable(false);
    button.setHorizontalTextPosition(SwingConstants.LEADING);
    return button;
  }

  private void createAndShowNewItemsMenu(NewResourceActionFactory actionMap,
                                         final JComponent component, final AbstractButton button) {
    JPopupMenu menu = new JPopupMenu();

    menu.setBorderPainted(true);
    menu.setBorder(BorderFactory.createEmptyBorder(0,24,0,0));


    Arrays.stream(ResourceType.values()).forEach(
        t -> {
          Action action = actionMap.getAction(t);
          if (action != null) {
            // TODO Add HOST_FACTORY if applicable
            action.putValue(Action.SMALL_ICON, Icons.getInstance().getIcon(t, 16, LIGHT_COLOR));
            AbstractButton b = new ToolBarButton(action).getButton();
            b.setHorizontalAlignment(SwingConstants.LEFT);
            menu.add(b, BorderLayout.WEST);
          }
        }
    );

    menu.setBackground(DARK_BG);
    menu.setForeground(Color.white);
    menu.addPopupMenuListener(new PopupMenuListener() {
      public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
      }

      public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        button.setSelected(false);
      }

      public void popupMenuCanceled(PopupMenuEvent e) {
        button.setSelected(false);
      }
    });
    menu.show(component, 0, component.getHeight());
  }


  private void search(DocumentEvent e) {
    if (actionListener != null) {
      actionListener.actionPerformed(new ActionEvent(
          searchTextField,
          Events.SERACH_RESOURCE,
          searchTextField.getText().trim()));
    }
  }

  private ImageIcon getLogoIcon() {
    URL resource = getClass().getClassLoader().getResource("conjur_4c_ko.png");

    return new ImageIcon(Objects.requireNonNull(resource));
  }

  public void setView(View view) {
    logger.trace("setView enter::setView {}", view);

    logger.debug("Clear search text");
    searchTextField.setText("");

    if (this.view != null) {
      logger.debug("Remove current view: {}", this.view.getType());
      mainView.remove(this.view.getComponent());
    }

    this.view = view;
    logger.debug("Handle navigation event");
    handleNavigationEvent(navigationsLabels.get(view.getType()));

    logger.debug("Wire select base actions, disabled in dashboard view");
    setButtonAction(editButton, ActionType.EditItem);
    setButtonAction(deleteButton, ActionType.DeleteItem);

    logger.debug("Add the new view");
    Component currentView = this.view.getComponent();
    mainView.add(currentView, BorderLayout.CENTER);
    mainView.revalidate();
    mainView.repaint();

    if (view instanceof ResourceView) {
      buildResourceMenu((ResourceView) view);
      resourceMenu.setEnabled(true);
    } else {
      resourceMenu.setEnabled(false);
    }

    logger.trace("setView exit::setView {}", view);
  }

  private void buildResourceMenu(ResourceView resourceView) {
    resourceMenu.removeAll();
    ActionMap resourceActions = resourceView.getResourceActions();
    Arrays.stream(ActionType.values())
      .filter(t -> resourceActions.get(t) != null)
      .forEach(type -> {
        Action action = resourceActions.get(type);
          if (resourceActions.size() > 2 && type == ActionType.DeleteItem) {
            resourceMenu.addSeparator();
          }

          resourceMenu.add(new JMenuItem(action));

          if (resourceActions.size() > 3 && type == ActionType.DeleteItem) {
            resourceMenu.addSeparator();
          }
        }
      );
  }

  private AbstractButton createToolBarButton(String text) {
    ToolBarButton b = new ToolBarButton(new JButton(text));
    b.getButton().setEnabled(false);

    return b.getButton();
  }

  private AbstractButton createToolBarButton(Action action) {
    return new ToolBarButton(action).getButton();
  }

  private void setButtonAction(AbstractButton button, ActionType type) {
    button.setEnabled(false);

    if (view instanceof ResourceView) {
      Action action = ((ResourceView)view).getAction(type);
      if (action != null) {
        button.setAction(action);
      }
    }
  }

  public View getComponentView() {
    return view;
  }

  private void handleNavigationEvent(NavigationLabel label) {
    if (selectedNavigationLabel != null) {
      selectedNavigationLabel.setSelectedState(false);
    }

    selectedNavigationLabel = label;

    if (label.getViewType() != this.view.getType()) {
      viewSelectedListener.viewSelected(label.getViewType());
    }

    selectedNavigationLabel.setSelectedState(true);
  }

  public void clearSearchText() {
    searchTextField.setText("");
  }

  public void onResourceSelected(ResourceModel resourceModel) {
    resourceMenu.setEnabled(resourceModel != null);
  }
}
