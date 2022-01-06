package com.cyberark.models.hostfactory;

import lombok.*;

@ToString
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@NoArgsConstructor
public class HostFactoryToken {
  @Getter(AccessLevel.PUBLIC) private String expiration;
  @Getter(AccessLevel.PUBLIC) private String token;
  @Getter(AccessLevel.PUBLIC) private String[] cidr;
}
