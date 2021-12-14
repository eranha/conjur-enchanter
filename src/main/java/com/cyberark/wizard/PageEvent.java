package com.cyberark.wizard;

/**
 * This class represents a wizard page event
 */
public class PageEvent {
  public enum EventType {
    AboutToHidePage,
    AboutToShowPage,
    AboutToFinish
  }

  private final EventType eventType;
  private final Page page;

  public PageEvent(EventType eventType, Page page) {
    this.eventType = eventType;
    this.page = page;
  }

  @Override
  public String toString() {
    return "PageEvent{" +
        "eventType=" + eventType +
        ", page=" + page +
        '}';
  }
}
