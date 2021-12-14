package com.cyberark.event;

import com.cyberark.models.ResourceModel;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class EventPublisher {
  private final static EventPublisher instance = new EventPublisher();
  private final Map<Class<? extends ResourceModel>,
      List<Consumer<ResourceEvent<? extends ResourceModel>>>> consumers = new HashMap<>();
  private final List<Consumer<ApplicationEvent>> appEventsConsumers = new ArrayList<>();
  private final List<Consumer<ActionEvent>> eventConsumers = new ArrayList<>();

  private EventPublisher() {
  }

  public static EventPublisher getInstance() {
    return instance;
  }



  public void addApplicationEventListener(Consumer<ApplicationEvent> c) {
    appEventsConsumers.add(c);
  }

  public void addListener(Consumer<ActionEvent> c) {
    if (!eventConsumers.contains(c)){
      eventConsumers.add(c);
    }
  }

  public void fireEvent(ResourceEvent<? extends ResourceModel> e) {
    if (e.getResource() != null && consumers.containsKey(e.getResource().getClass())) {
      consumers.get(e.getResource().getClass()).forEach(c -> c.accept(e));
    }

    fireEvent(new ActionEvent(e, ActionEvent.ACTION_FIRST, ""));
  }

  public void fireEvent(ActionEvent e) {
    eventConsumers.forEach(c -> c.accept(e));
  }

  public void fireEvent(ApplicationEvent event) {
    appEventsConsumers.forEach(c -> c.accept(event));
  }
}
