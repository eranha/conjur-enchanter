package com.cyberark.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResourceIdentifierTest {

  @Test
  void deriveFromNullIdentifierShouldRaiseError() {
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      ResourceIdentifier.deriveFrom(null, "id");
    });

    String expectedMessage = "identifier";
    String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  void deriveFrom() {
    ResourceIdentifier template = ResourceIdentifier.fromString("foo:layer:template");
    assertEquals(ResourceIdentifier.fromString("foo:layer:id"), ResourceIdentifier.deriveFrom(template, "id"));
  }

  @Test
  void deriveFromStringOverloadNullAccountShouldRaiseError() {
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      ResourceIdentifier.fromString(null, (ResourceType) null, null);
    });

    String expectedMessage = "account";
    String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  void deriveFromStringOverloadEmptyAccountShouldRaiseError() {
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      ResourceIdentifier.fromString("", (ResourceType) null, null);
    });

    String expectedMessage = "account";
    String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  void deriveFromStringOverloadNullTypeShouldRaiseError() {
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      ResourceIdentifier.fromString("account", (ResourceType) null, null);
    });

    String expectedMessage = "type";
    String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  void deriveFromStringOverloadNullStringTypeShouldRaiseError() {
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      ResourceIdentifier.fromString("account", (String)null, null);
    });

    String expectedMessage = "type";
    String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  void deriveFromStringOverloadEmptyStringTypeShouldRaiseError() {
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      ResourceIdentifier.fromString("account", "", null);
    });

    String expectedMessage = "type";
    String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  void deriveFromStringOverloadInvalidStringTypeShouldRaiseError() {
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      ResourceIdentifier.fromString("account", "foo", null);
    });
  }

  @Test
  void deriveFromStringOverloadNullIdShouldRaiseError() {
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      ResourceIdentifier.fromString("account", ResourceType.layer, null);
    });

    String expectedMessage = "id";
    String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  void deriveFromStringOverloadEmptyIdShouldRaiseError() {
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      ResourceIdentifier.fromString("account", ResourceType.layer, "");
    });

    String expectedMessage = "id";
    String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  void deriveFromNullIdShouldRaiseError() {
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      ResourceIdentifier.deriveFrom(ResourceIdentifier.fromString("foo:layer:template"), null);
    });

    String expectedMessage = "id";
    String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  void deriveFromEmptyIdShouldRaiseError() {
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      ResourceIdentifier.deriveFrom(ResourceIdentifier.fromString("foo:layer:template"), "");
    });

    String expectedMessage = "id";
    String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  void nullIdentifierShouldRaiseError() {
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      ResourceIdentifier.fromString(null);
    });

    String expectedMessage = "identifier";
    String actualMessage = exception.getMessage();

        assertEquals(expectedMessage, actualMessage);
  }

  @Test
  void emptyIdentifierShouldRaiseError() {
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      ResourceIdentifier.fromString("");
    });

    String expectedMessage = "identifier";
    String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  void missingAccountShouldRaiseError() {
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      ResourceIdentifier.fromString("user:bob");
    });

    String expectedMessage = "user:bob";
    String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  void testEquals() {
    assertEquals(ResourceIdentifier.fromString("test:user:bob"), ResourceIdentifier.fromString("test:user:bob"));
  }

  @Test
  void emptyIdShouldRaiseError() {
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      ResourceIdentifier.fromString("test:user:");
    });

    String expectedMessage = "test:user:";
    String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  void emptyAccountShouldRaiseError() {
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      ResourceIdentifier.fromString(":user:bob");
    });

    String expectedMessage = ":user:bob";
    String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  void missingTypeShouldRaiseError() {
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      ResourceIdentifier.fromString("test::bob");
    });

    String expectedMessage = "No enum constant com.cyberark.models.ResourceType.";
    String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  void invalidIdentifierFormatShouldRaiseError() {
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      ResourceIdentifier.fromString("bob");
    });

    String expectedMessage = "bob";
    String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  void invalidIdentifierTypeShouldRaiseError() {
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      ResourceIdentifier.fromString("test:foo:bar");
    });

    String expectedMessage = "No enum constant com.cyberark.models.ResourceType.foo";
    String actualMessage = exception.getMessage();

    assertEquals(expectedMessage, actualMessage);
  }

  @Test
  void fromString() {
    ResourceIdentifier i = ResourceIdentifier.fromString("test:user:bob");
    assertNotNull(i);
  }

  @Test
  void getAccount() {
    ResourceIdentifier i = ResourceIdentifier.fromString("test:user:bob");
    assertEquals("test", i.getAccount());
  }

  @Test
  void getType() {
    ResourceIdentifier i = ResourceIdentifier.fromString("test:user:bob");
    assertEquals(ResourceType.user, i.getType());
  }

  @Test
  void getId() {
    ResourceIdentifier i = ResourceIdentifier.fromString("test:user:bob");
    assertEquals("bob", i.getId());
  }

  @Test
  void getFullyQualifiedId() {
    ResourceIdentifier i = ResourceIdentifier.fromString("test:user:bob");
    assertEquals("test:user:bob", i.getFullyQualifiedId());
  }
}