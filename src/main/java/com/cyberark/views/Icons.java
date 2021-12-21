package com.cyberark.views;

import com.cyberark.models.ResourceType;
import jiconfont.DefaultIconCode;
import jiconfont.IconCode;
import jiconfont.IconFont;
import jiconfont.swing.IconFontSwing;

import javax.swing.*;
import java.awt.*;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static com.cyberark.Consts.LIGHT_COLOR;

public class Icons {
  public static final char POLICY_ICON_UNICODE = '\ue826';
  public static final char HOST_ICON_UNICODE = '\ue810';
  public static final char LAYER_ICON_UNICODE = '\ue811';
  public static final char USERS_ICON_UNICODE = '\ue805';
  public static final char GROUPS_ICON_UNICODE = '\ue806';
  public static final char SECRETS_ICON_UNICODE = '\ue800';
  public static final char WEBSERVICES_ICON_UNICODE = '\ue80d';
  public static final char SEARCH_ICON_UNICODE = '\ue802';
  public static final char LOCK_ICON_UNICODE = '\ue809';
  public static final char DASHBOARD_ICON_UNICODE = '\ue807';
  public static final char ICON_INFO = '\ue803';
  public static final char ICON_LOCK = '\ue809';
  public static final char ICON_SPIN = '\ue801';
  public static final char ICON_HOST_ROTATOR = '\ue814';
  public static final char ICON_UP_OPEN = '\ue80e';
  public static final char ICON_CUSTOM_TYPES = '\ue80f';
  public static final char ICON_COG = '\ue817';
  public static final char ICON_PUPPET = '\ue81a';
  public static final char ICON_MOVE = '\ue812';
  public static final char ICON_DOWN_OPEN = '\ue813';
  public static final char ICON_CIRCLE = '\uf111';

  public static final int DEFAULT_FONT_SIZE = 24;
  private static Icons instance;
  private final Map<Character, IconCode> cache = new HashMap<>();

  private Icons() {
    IconFontSwing.register(new IconFont() {
      @Override
      public String getFontFamily() {
        return "fontello";
      }

      @Override
      public InputStream getFontInputStream() {
        return Icons.class.getResourceAsStream("/font/fontello.ttf");
      }
    });
  }

  public Icon getIcon(ViewType type, int size, Color foreground) {
    switch (type) {
      case Dashboard:
        return getIcon(DASHBOARD_ICON_UNICODE, size, foreground);
      case Policies:
        return getIcon(POLICY_ICON_UNICODE, size, foreground);
      case Hosts:
        return getIcon(HOST_ICON_UNICODE, size, foreground);
      case Layers:
        return getIcon(LAYER_ICON_UNICODE, size, foreground);
      case Users:
        return getIcon(USERS_ICON_UNICODE, size, foreground);
      case Groups:
        return getIcon(GROUPS_ICON_UNICODE, size, foreground);
      case Secrets:
        return getIcon(SECRETS_ICON_UNICODE, size, foreground);
      case Webserivices:
        return getIcon(WEBSERVICES_ICON_UNICODE, size, foreground);    }
    return null;
  }

  public Icon getIcon(ResourceType type, int size, Color foreground) {

    switch (type) {
      case policy:
        return getIcon(POLICY_ICON_UNICODE, size, foreground);
      case host:
        return getIcon(HOST_ICON_UNICODE, size, foreground);
      case layer:
        return getIcon(LAYER_ICON_UNICODE, size, foreground);
      case user:
        return getIcon(USERS_ICON_UNICODE, size, foreground);
      case group:
        return getIcon(GROUPS_ICON_UNICODE, size, foreground);
      case variable:
        return getIcon(SECRETS_ICON_UNICODE, size, foreground);
      case webservice:
        return getIcon(WEBSERVICES_ICON_UNICODE, size, foreground);
      case host_factory:
        return getIcon(ICON_HOST_ROTATOR, size, foreground);
    }
    return null;
  }


  public static Icons getInstance() {
    if (instance == null) {
      instance = new Icons();
    }
    return instance;
  }

  public Icon getIcon(ResourceType type) {
    return getIcon(type, DEFAULT_FONT_SIZE, LIGHT_COLOR);
  }

  public Icon getIcon(ViewType type) {
    return getIcon(type, DEFAULT_FONT_SIZE, LIGHT_COLOR);
  }

  public Icon getIcon(char unicode, int fontSize, Color foreground) {
    cache.computeIfAbsent(unicode, v -> new DefaultIconCode("fontello", unicode));
    return IconFontSwing.buildIcon(cache.get(unicode), fontSize, foreground);
  }
}
