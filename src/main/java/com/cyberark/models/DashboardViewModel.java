package com.cyberark.models;

import com.cyberark.models.audit.AuditEvent;

import java.util.List;
import java.util.Map;

public class DashboardViewModel implements ViewModel {
  private final List<AuditEvent> auditEvents;
  private final Map<ResourceType, Integer> resourceCount;

  public DashboardViewModel(List<AuditEvent> auditEvents, Map<ResourceType, Integer> resourceCount) {
    this.auditEvents = auditEvents;
    this.resourceCount = resourceCount;
  }

  public List<AuditEvent> getAuditEvents() {
    return auditEvents;
  }

  public Map<ResourceType, Integer> getResourceCount() {
    return resourceCount;
  }
}
