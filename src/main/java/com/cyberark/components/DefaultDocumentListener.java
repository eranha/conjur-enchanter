package com.cyberark.components;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.function.Consumer;

public class DefaultDocumentListener implements DocumentListener {
  private final Consumer<DocumentEvent> consumer;

  public DefaultDocumentListener(Consumer<DocumentEvent> consumer) {
    this.consumer = consumer;
  }

  @Override
    public void insertUpdate(DocumentEvent e) {
      fireEvent(e);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
      fireEvent(e);
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
      fireEvent(e);
    }

    void fireEvent(DocumentEvent e) {
      if (consumer != null) {
        consumer.accept(e);
      }
    }
}
