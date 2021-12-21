package com.cyberark.actions;

import com.cyberark.Application;
import com.cyberark.Consts;
import com.cyberark.Util;
import com.cyberark.components.*;
import com.cyberark.event.EventPublisher;
import com.cyberark.event.ResourceEvent;
import com.cyberark.exceptions.ResourceAccessException;
import com.cyberark.models.*;
import com.cyberark.models.table.StringTableModel;
import com.cyberark.resource.ResourceServiceFactory;
import com.cyberark.resource.ResourcesService;
import com.cyberark.views.ErrorView;
import com.cyberark.views.Icons;
import com.cyberark.views.ResourceFormView;
import com.cyberark.wizard.Page;
import com.cyberark.wizard.Wizard;
import com.fasterxml.jackson.core.JsonProcessingException;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.cyberark.Consts.*;

public class NewResourceAction extends AbstractAction {

  private static final List<ResourceType> ROLE_RESOURCE_TYPE = Arrays.stream(
      (new ResourceType[]{
          ResourceType.user,
          ResourceType.host,
          ResourceType.group,
          ResourceType.layer})
  ).collect(Collectors.toList());

  private final ResourceType resourceType;

  private enum PageType {
    General,
    Privileges,
    Grants,
    Restrictions
  }


  public NewResourceAction(ResourceType type, int mnemonicKey, KeyStroke keyStroke) {
    this(Util.resourceTypeToTitle(type), type, mnemonicKey, keyStroke);
  }

  public NewResourceAction(String text, ResourceType type, int mnemonicKey, KeyStroke keyStroke) {
    super(text);
    this.resourceType = type;
    putValue(SHORT_DESCRIPTION, String.format("Add new '%s' resource", type.toString()));
    putValue(MNEMONIC_KEY, mnemonicKey);
    putValue(MNEMONIC_KEY, mnemonicKey);
    putValue(Action.ACCELERATOR_KEY, keyStroke);
    putValue(ACTION_TYPE_KEY, ActionType.NewItem);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    String resourceTypeTitle =  String.format(
        "Add New %s",
        resourceTypeToTitle(Objects.requireNonNull(resourceType))
    );

    String title = String.format(
        "%s - Add %s",
        Consts.APP_NAME, resourceTypeTitle
    );

    try {
      Properties pageInfo = new Properties();

      pageInfo.load(getWizardPagesInfoProperties());

      List<Page> pages = getWizardPages(
          resourceType, getResourcesService().getResourceIdentifiers(),
          pageInfo);

      Component component = getPageComponent(PageType.General, pages);
      ResourceFormView resourceView = (component instanceof ResourceFormView)
          ? (ResourceFormView)component
          : null;

      // Return true if the user provides an id to the resource
      // used to toggle the Wizard OK button
      Predicate<Void> wizardCanFinish = v -> resourceView == null || Util.stringIsNotNullOrEmpty(resourceView.getId());

      Wizard whiz = new Wizard(
          Icons.getInstance().getIcon(resourceType, 48, DARK_BG),
          title,
          System.out::println,
          pages,
          wizardCanFinish);

      if (whiz.showWizard(getMainForm()) == Wizard.OK_OPTION) {
        onNewResourceWizardFinish(resourceType, pages);
      }
    } catch (ResourceAccessException | IOException ex) {
      ex.printStackTrace();
      ErrorView.showErrorMessage(ex);
    }
  }

  private InputStream getWizardPagesInfoProperties() throws FileNotFoundException {
    return Util.getProperties(RESOURCES_INFO_PROPERTIES);
  }

  protected void showResponse(ResourceIdentifier model, String response) {
    switch (model.getType()) {
      case variable:
      case webservice:
      case layer:
      case group:
        JOptionPane.showMessageDialog(getMainForm(), new JScrollPane(new JTextArea(response)));
        break;
      case user:
      case host:
        promptToCopyApiKeyToClipboard(response, model);
        break;
    }
  }

  private void promptToCopyApiKeyToClipboard(String response, ResourceIdentifier model) {
    String apiKey;

    try {
      apiKey = Util.extractApiKey(
          response,
          model.getFullyQualifiedId()
      );
    } catch (JsonProcessingException e) {
      /* response is the api key not a json */
      apiKey = response;
    }

    if (apiKey == null) {
      JOptionPane
          .showMessageDialog(getMainForm(),
              String.format("Rotate the API key of role '%s' to get the its value.", model.getId()),
              "API Key",
              JOptionPane.INFORMATION_MESSAGE);
      return;
    }

    JLabel label = new JLabel("Click Copy to copy the API key to clipboard.");
    JLabel label2 = new JLabel("(you will only see this once)");
    JTextField jt = new JTextField(apiKey);
    jt.addAncestorListener(new AncestorListener()
    {
      public void ancestorAdded ( AncestorEvent event )
      {
        jt.requestFocus();
        jt.selectAll();
      }
      public void ancestorRemoved ( AncestorEvent event ) {}
      public void ancestorMoved ( AncestorEvent event ) {}
    });

    Object[] choices = {"Copy", "Close"};
    Object defaultChoice = choices[0];

    int answer = JOptionPane
        .showOptionDialog(getMainForm(), new Component[]{label, label2,jt}, "Copy API Key to Clipboard?",
            JOptionPane.YES_NO_CANCEL_OPTION,//
            JOptionPane.QUESTION_MESSAGE, null, choices, defaultChoice);

    if (answer == JOptionPane.YES_OPTION) {
      Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      clipboard.setContents(new StringSelection(apiKey), null);
    }
  }

  protected Frame getMainForm() {
    return Application.getInstance().getMainForm();
  }

  private List<Page> getWizardPages(ResourceType resourceType,
                                    List<ResourceIdentifier> resources,
                                    Properties pageInfo) {
    List<Page> pages = null;

    switch (resourceType) {
      case variable:
        pages = getNewVariablePages(resourceType, resources, pageInfo);
        break;
      case webservice:
        pages = getNewResourcePages(resourceType, resources, pageInfo);
        break;
      case user:
      case host:
        pages = getNewRolePages(resourceType, resources, pageInfo);
        break;
      case layer:
      case group:
        pages = getNewSetResourcePages(resourceType, resources, pageInfo);
        break;
    }

    return pages;
  }

  private void onNewResourceWizardFinish(ResourceType resourceType,
                                         List<Page> pages) throws ResourceAccessException {
    String response;
    ResourceModel model;

    model = getNewModel(resourceType);
    populateModel(resourceType, model, (ResourceFormView) getPageComponent(PageType.General, pages));
    response = addResource(resourceType, pages, model);

    // set permissions in a separate call
    setPermissions(model, (PrivilegesPanel) getPageComponent(PageType.Privileges, pages));

    if (response != null) {
      showResponse(model.getIdentifier(), response);
    }

    EventPublisher.getInstance().fireEvent(new ResourceEvent<>(model));
  }

  private String addResource(ResourceType resourceType,
                             List<Page> pages,
                             ResourceModel model) throws ResourceAccessException {
    String response = null;

    switch (resourceType) {
      case variable:
      case webservice:
        // nothing special here just add the resource
        response = getResourcesService().addResource(resourceType, model);
        break;
      case user:
      case host:
        response = addRole(resourceType, pages, (RoleModel) model);
        break;
      case layer:
      case group:
        // collect members (roles that are members of the new role was granted membership)
        List<ResourceIdentifier> members = getGrants(pages);
        response = getResourcesService().addResource(resourceType, model, members);
        break;
    }
    return response;
  }

  private String addRole(ResourceType resourceType, List<Page> pages, RoleModel model) throws ResourceAccessException {
    String response;
    // collect memberships (roles that the new role was granted membership)
    List<ResourceIdentifier> grantedSetRoles = getGrants(pages);

    // set restrictions
    Component component = getPageComponent(PageType.Restrictions, pages);
    EditableTable table = (component instanceof EditableTable) ? (EditableTable)component : null;
    model.setRestrictedTo(Objects.requireNonNull(table).getModel() instanceof StringTableModel
        ? ((StringTableModel)table.getModel()).getItems()
        : new String[0]);

    response = getResourcesService().addRole(resourceType, model, grantedSetRoles);
    return response;
  }

  protected ResourcesService getResourcesService() {
    return ResourceServiceFactory.getInstance().getResourcesService();
  }


  private List<ResourceIdentifier> getGrants(List<Page> pages) {
    ItemsSelector selector = (ItemsSelector) getPageComponent(PageType.Grants, pages);
    return selector.getSelectedItems();
  }

  private Component getPageComponent(PageType type, List<Page> pages) {
    return pages.stream().filter(p -> type.toString().equals(p.getId()))
        .findFirst()
        .map(Page::getPageView)
        .orElse(null);
  }

  private void setPermissions(ResourceModel model, PrivilegesPanel privilegesPane) throws ResourceAccessException {
    Map<ResourceIdentifier, Set<String>> privileges = privilegesPane.getPrivileges();
    if (!privileges.isEmpty()) {
      if (model.getIdentifier().getType() != ResourceType.webservice
          && model.getIdentifier().getType() != ResourceType.variable) {
        // model is the role that is gaining access to the resource.
        getResourcesService().permit(model, privileges);
      } else {
        // in case of variable and webservice resources the model is the resource
        // permit each resource entry in the map with the corresponding privileges
        getResourcesService().permit(privileges, model);
      }
    }
  }

  private ResourceModel getNewModel(ResourceType resourceType) {
    ResourceModel model = null;

    switch (resourceType) {
      case variable:
        model = new SecretModel();
        break;
      case user:
      case host:
        model = new RoleModel();
        break;
      case webservice:
      case layer:
      case group:
        model = new ResourceModel();
        break;
    }

    return model;
  }

  private <T extends ResourceModel> void populateModel(ResourceType resourceType, T model, ResourceFormView view) {
    model.setId(getResourceFullyQualifiedId(resourceType, view.getId()));
    model.setOwner(view.getOwner());
    model.setPolicy(view.getPolicy());
    model.setAnnotations(view.getAnnotations().toArray(new Annotation[0]));
  }
  
  private static String getResourceFullyQualifiedId(ResourceType resourceType, String id) {
    return String.format(
        "%s:%s:%s",
        Application.getInstance().getAccount(),
        resourceType,
        id);
  }

  // Set of wizard pages for a new set resource
  // A set resource is either layer or group
  private List<Page> getNewSetResourcePages(ResourceType resourceType,
                                            List<ResourceIdentifier> resources,
                                            Properties pageInfo) {
    List<Page> pages = new ArrayList<>();

    // General
    pages.add(getGeneralPage(resourceType, resources, pageInfo));
    // Permissions
    pages.add(getSetResourceGrantsPage(resourceType, resources, pageInfo));
    // Privileges
    pages.add(getPrivilegesPage(pageInfo, resourceType, filter(resources, ROLE_RESOURCE_TYPE::contains)));

    return pages;
  }

  // Set of wizard pages for a new role
  // A role is either user or host
  private List<Page> getNewRolePages(ResourceType resourceType,
                                     List<ResourceIdentifier> resources,
                                     Properties pageInfo) {
    List<Page> pages = new ArrayList<>();

    // General
    pages.add(getGeneralPage(resourceType, resources, pageInfo));
    // Permissions
    pages.add(getRoleGrantsPage(resourceType, resources, pageInfo));
    // Restrictions
    pages.add(getRestrictionsPage(pageInfo));
    // Privileges
    pages.add(getPrivilegesPage(pageInfo, resourceType, filter(resources, ROLE_RESOURCE_TYPE::contains)));

    return pages;
  }

  private Page getRestrictionsPage(Properties pageInfo) {
    return new Page(
        PageType.Restrictions.toString(),
        "Restrictions", pageInfo.getProperty("restrictions"),
        new EditableTableImpl<>(
            new StringTableModel(), m -> "0.0.0.0", false
        )
    );
  }

  // Set of wizard pages for a new resource
  // Applies to resources of type webservice and variable
  // TODO consider move new policy to here?
  private List<Page> getNewResourcePages(ResourceType resourceType,
                                         List<ResourceIdentifier> resources,
                                         Properties pageInfo) {
    List<Page> pages = new ArrayList<>();

    // General
    pages.add(getGeneralPage(resourceType, resources, pageInfo));

    // Privileges
    pages.add(getPrivilegesPage(pageInfo, resourceType, filter(resources, ROLE_RESOURCE_TYPE::contains)));

    return pages;
  }


  private List<Page> getNewVariablePages(ResourceType resourceType,
                                         List<ResourceIdentifier> resources,
                                         Properties pageInfo) {
    List<Page> pages = new ArrayList<>();

    // General
    pages.add(getVariableGeneralPage(resourceType, resources, pageInfo));

    // Privileges
    pages.add(getPrivilegesPage(pageInfo, resourceType, filter(resources, ROLE_RESOURCE_TYPE::contains)));

    return pages;
  }

  // permit roles
  // Required. Identifies the role that is gaining access to the resource.
  // policy, group, user, layer, host.

  // permit resources
  // Required. Identifies the resource whose access is being controlled.
  // policy, user, host, group, layer, variable, webservice.
  private Page getPrivilegesPage(Properties pageInfo,
                                 ResourceType resourceType,
                                 List<ResourceIdentifier> roles) {
    return new Page(
        PageType.Privileges.toString(),
        "Permissions",
        pageInfo.getProperty("role.privileges"),
        new PrivilegesPanel(
            !(Util.isRoleResource(resourceType) || Util.isSetResource(resourceType))
              ? "Roles" : "Resources",
            resourceType, new Permission[0], roles));
  }

  private Page getSetResourceGrantsPage(ResourceType type, List<ResourceIdentifier> resources, Properties pageInfo) {
    return new Page(
        PageType.Grants.toString(),
        "Members",
        pageInfo.getProperty("role.members"),
        new ItemsSelector(
          filter(
              resources,
              type == ResourceType.group
                ? (t -> t == ResourceType.user || t == ResourceType.group)
                : (t -> t == ResourceType.host || t == ResourceType.layer)
          ),
          new ArrayList<>()
        )
    );
  }

  private Page getRoleGrantsPage(ResourceType type, List<ResourceIdentifier> resources, Properties pageInfo) {
    return new Page(
        PageType.Grants.toString(),
        type == ResourceType.user ? "Groups" : "Layers",
        pageInfo.getProperty("role.members"),
        new ItemsSelector(filter(resources,
            type == ResourceType.user
                ? (t -> t == ResourceType.group)
                : (t -> t == ResourceType.layer)),
        new ArrayList<>()));
  }

  private Page getGeneralPage(ResourceType type, List<ResourceIdentifier> resources, Properties pageInfo) {
    return new Page(
        PageType.General.toString(),
        "General",
        pageInfo.getProperty(type.toString()),
        new ResourceForm(
            type,
            resources,
            filter(resources, ROLE_RESOURCE_TYPE::contains)
        )
    );
  }

  private Page getVariableGeneralPage(ResourceType type,
                                      List<ResourceIdentifier> resources,
                                      Properties pageInfo) {
    return new Page(
        PageType.General.toString(),
        "General",
        pageInfo.getProperty(type.toString()),
        new SecretForm(resources)
    );
  }

  private static List<ResourceIdentifier> filter(List<ResourceIdentifier> resources,
                                                 Predicate<ResourceType> resourceTypePredicate) {
    return resources
        .stream()
        .filter(r -> resourceTypePredicate.test(r.getType()))
        .collect(Collectors.toList());
  }

  private String resourceTypeToTitle(ResourceType type) {
    return Util.resourceTypeToTitle(type);
  }
}
