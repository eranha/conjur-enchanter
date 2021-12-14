package com.cyberark.views;

import java.awt.*;

public interface MessageView {
  void showMessageDialog(String message);
  void showMessageDialog(Object message, String title, int messageType);
}
