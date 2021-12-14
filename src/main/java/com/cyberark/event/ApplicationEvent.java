package com.cyberark.event;

public class ApplicationEvent {
  private final EventType eventType;

  public ApplicationEvent(EventType eventType) {
    this.eventType = eventType;
  }

  public EventType getEventType() {
    return eventType;
  }
}
