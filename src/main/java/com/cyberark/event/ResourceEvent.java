package com.cyberark.event;

import com.cyberark.models.ResourceModel;

public class ResourceEvent<T extends ResourceModel> {
  private final T resource;

  public ResourceEvent(T resource) {
    this.resource = resource;
  }

  public T getResource() { return resource; }
}
