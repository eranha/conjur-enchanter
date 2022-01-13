package com.cyberark.components;

import com.cyberark.Util;

import javax.swing.*;
import java.awt.*;

public class JsonViewer {
  public static void showDialog(Component parentComponent, String title, String json) {
    String indented = Util.prettyPrintJson(json);
    JPanel panel = new JPanel();
    JTextArea jt = new JTextArea(indented);

    panel.setBorder(BorderFactory.createEmptyBorder(0,0,0,4));
    panel.add(new JScrollPane(jt));

    JOptionPane.showMessageDialog(
        parentComponent,
        panel,
        title,
        JOptionPane.INFORMATION_MESSAGE
    );
  }
}
