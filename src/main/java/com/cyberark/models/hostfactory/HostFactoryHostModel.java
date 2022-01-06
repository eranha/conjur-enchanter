package com.cyberark.models.hostfactory;

import com.cyberark.models.Annotation;
import lombok.*;

@ToString
@AllArgsConstructor
public class HostFactoryHostModel {
  @Getter(AccessLevel.PUBLIC)
  @NonNull
  private String hostName;

  @Getter(AccessLevel.PUBLIC)
  @NonNull
  private HostFactoryToken hostFactoryToken;

  @Getter(AccessLevel.PUBLIC)
  @NonNull
  private Annotation[] annotations;
}
