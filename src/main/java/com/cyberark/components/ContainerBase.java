package com.cyberark.components;

import com.cyberark.util.Resources;

import javax.swing.*;

abstract class ContainerBase extends JPanel {
  protected static String getString(String key) {
    return Resources.getString(key);
  }
}
