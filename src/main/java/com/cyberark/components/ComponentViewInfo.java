package com.cyberark.components;

import java.awt.*;

public class ComponentViewInfo {
  String label;
  Component component;
  int labelAnchor;
  int labelFill;
  int componentFill;
  int componentWeightX;
  int componentWeightY;
  Dimension compSize;

  public ComponentViewInfo(String label, Component component, int labelAnchor, int labelFill, int componentFill,
                           int componentWeightX, int componentWeightY, Dimension compSize) {
    this.label = label;
    this.component = component;
    this.labelAnchor = labelAnchor;
    this.labelFill = labelFill;
    this.componentFill = componentFill;
    this.componentWeightX = componentWeightX;
    this.componentWeightY = componentWeightY;
    this.compSize = compSize;
  }
}
