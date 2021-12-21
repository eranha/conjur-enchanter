package com.cyberark;

import java.awt.*;

/**
 * Applicaiton constants interface.
 */
public interface Consts {
  Color DARK_BG = new Color(14, 34, 40);
  Font LABEL_FONT = new Font("Verdana", Font.PLAIN, 14);
  Color LABEL_FOREGROUND = Color.WHITE;
  Color LIGHT_COLOR = new Color(53, 197, 193);
  Color CYBR_BLUE = new Color(47, 115, 176);

  /** Return value from class method if CANCEL is chosen. */
  int         OK_OPTION = 0;
  /** Return value from class method if CANCEL is chosen. */
  int         CANCEL_OPTION = 2;

  String ACTION_TYPE_KEY = "action.type";
  String RESOURCE_TYPE_KEY = "resource.type";
  String APP_NAME = "Enchanter";
  String ROOT = "root";

  enum PolicyApiMode {
    Post,
    Patch,
    Put
  }

  String RESOURCES_INFO_PROPERTIES = "/resources_info.properties";
  String HTML_PARAGRAPH = "<html><body style='width: %1spx'>%1s</body></html>";
}
