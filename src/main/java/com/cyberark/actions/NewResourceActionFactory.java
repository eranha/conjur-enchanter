package com.cyberark.actions;

import com.cyberark.models.ResourceType;
import com.cyberark.resource.ResourceServiceFactory;
import com.cyberark.views.ViewFactory;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.HashMap;

/**
 * Factory Class for new resource actions
 */
public class NewResourceActionFactory {
  private final HashMap<ResourceType, Action> actions = new HashMap<>();

  public NewResourceActionFactory() {
    actions.put(ResourceType.policy, new NewPolicyAction(
        ResourceServiceFactory.getInstance().getResourcesService(),
        ViewFactory.getInstance().getMessageView()
    ));
    actions.put(ResourceType.user, new NewResourceAction(
        ResourceType.user,
        KeyEvent.VK_R,
        KeyStroke.getKeyStroke(KeyEvent.VK_U, KeyEvent.CTRL_DOWN_MASK)));
    actions.put(ResourceType.host, new NewResourceAction(
        ResourceType.host,
        KeyEvent.VK_H,
        KeyStroke.getKeyStroke(KeyEvent.VK_H, KeyEvent.CTRL_DOWN_MASK)));
    actions.put(ResourceType.layer, new NewResourceAction(
        ResourceType.layer,
        KeyEvent.VK_L,
        KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.CTRL_DOWN_MASK)));
    actions.put(ResourceType.group, new NewResourceAction(
        ResourceType.group,
        KeyEvent.VK_G,
        KeyStroke.getKeyStroke(KeyEvent.VK_G, KeyEvent.CTRL_DOWN_MASK)));
    actions.put(ResourceType.variable, new NewResourceAction(
        ResourceType.variable,
        KeyEvent.VK_E,
        KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_DOWN_MASK)));
    actions.put(ResourceType.webservice, new NewResourceAction(
        ResourceType.webservice,
        KeyEvent.VK_W,
        KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.CTRL_DOWN_MASK)));
    actions.put(ResourceType.host_factory, new NewResourceAction(
        ResourceType.host_factory,
        KeyEvent.VK_T,
        KeyStroke.getKeyStroke(KeyEvent.VK_T, KeyEvent.CTRL_DOWN_MASK)));
  }

  public Action getAction(ResourceType type) {
    return actions.get(type);
  }
}
