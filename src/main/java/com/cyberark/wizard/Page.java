package com.cyberark.wizard;

import java.awt.*;
import java.util.UUID;

/**
 * This class represents a wizard page model
 */
public class Page {
  private final String title;
  private final String description;
  private final boolean mandatory;

  @Override
  public String toString() {
    return "Page{" +
        "id='" + id + '\'' +
        ", title='" + title + '\'' +
        ", description='" + ( (description != null && description.length() > 10)
          ? (description.substring(0, 10) + "....")
          : description) + '\'' +
        ", mandatory=" + mandatory +
        ", id='" + id + '\'' +
        ", pageView=" + pageView.getClass().getName() +
        '}';
  }

  private final String id;

  // Page View
  private final Component pageView;

  public Page(String title, String description, Component pageView) {
    this(UUID.randomUUID().toString(), title, description, pageView, false);
  }

  public Page(String id, String title, String description, Component pageView) {
    this(id, title, description, pageView, false);
  }

  public Page(String id, String title, String description, Component pageView, boolean mandatory) {
    this.id = id;
    this.title = title;
    this.description = description;
    this.pageView = pageView;
    this.mandatory = mandatory;
  }

  public String getTitle() {
    return title;
  }

  public Component getPageView() {
    return pageView;
  }

  public String getDescription() {
    return description;
  }

  public boolean isMandatory() {
    return mandatory;
  }

  public String getId() {
    return id;
  }
}
