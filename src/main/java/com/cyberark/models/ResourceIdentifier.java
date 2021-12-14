package com.cyberark.models;

import com.cyberark.Util;

import java.util.Objects;

public class ResourceIdentifier {
  private final String account;
  private final ResourceType type;
  private final String id;
  private final String fullyQualifiedId;

  private ResourceIdentifier(String account, ResourceType type, String id, String fullyQualifiedId) {
    this.account = account;
    this.type = type;
    this.id = id;
    this.fullyQualifiedId = fullyQualifiedId;
  }

  public static ResourceIdentifier fromString(String account, String type, String id) {
    if (type == null) {
      throw new IllegalArgumentException("type");
    }

    if (Util.isNullOrEmptyString(type)) {
      throw new IllegalArgumentException("type");
    }

    return fromString(account, ResourceType.valueOf(type), id);
  }

  public static ResourceIdentifier fromString(String account, ResourceType type, String id) {
    if (Util.isNullOrEmptyString(account)) {
      throw new IllegalArgumentException("account");
    }

    if (type == null) {
      throw new IllegalArgumentException("type");
    }

    if (Util.isNullOrEmptyString(id)) {
      throw new IllegalArgumentException("id");
    }

    return new ResourceIdentifier(account, type, id, String.format("%s:%s:%s", account, type, id));
  }

  public static ResourceIdentifier deriveFrom(ResourceIdentifier identifier, String id) {
    if (identifier == null) {
      throw new IllegalArgumentException("identifier");
    }

    if (Util.isNullOrEmptyString(id)) {
      throw new IllegalArgumentException("id");
    }

    return new ResourceIdentifier(identifier.account, identifier.type, id, String.format("%s:%s:%s",
        identifier.account, identifier.type, id));

  }

  public static ResourceIdentifier fromString(String identifier) {
    if (Util.isNullOrEmptyString(identifier)) {
      throw new IllegalArgumentException("identifier");
    }

    String[] tokens = identifier.split(":");

    if (tokens.length >= 3) {
      if (Util.isNullOrEmptyString(tokens[0]) || Util.isNullOrEmptyString(tokens[2])) {
        throw new IllegalArgumentException(identifier);
      }

      return new ResourceIdentifier(tokens[0], ResourceType.valueOf(tokens[1]),
          identifier.substring(
              tokens[0].length() + 1 + tokens[1].length() + 1
              ), identifier);
    }

    throw new IllegalArgumentException(identifier);
  }

  public String getAccount() {
    return account;
  }

  public ResourceType getType() {
    return type;
  }

  public String getId() {
    return id;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ResourceIdentifier that = (ResourceIdentifier) o;
    return account.equals(that.account) && type == that.type && id.equals(that.id);
  }
  @Override
  public int hashCode() {
    return Objects.hash(account, type, id);
  }

  @Override
  public String toString() {
    return "ResourceIdentifier{" +
        "account='" + account + '\'' +
        ", type=" + type +
        ", id='" + id + '\'' +
        ", fullyQualifiedId='" + fullyQualifiedId + '\'' +
        '}';
  }

  public String getFullyQualifiedId() {
    return fullyQualifiedId;
  }
}
