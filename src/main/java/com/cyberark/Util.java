package com.cyberark;

import com.cyberark.models.ResourceType;
import com.cyberark.views.ViewType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ocpsoft.prettytime.PrettyTime;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.cyberark.Consts.ISO_8601_24H_FULL_FORMAT;

/**
 * Utility functions
 */
public class Util {

  /**
   * Capitalize the first char pf the type and returns it as a string
   * @param type the type to capitalize
   * @return type as string first letter in capital
   */
  public static String resourceTypeToTitle(ResourceType type) {
    return type == ResourceType.host_factory
        ? "Host Factory"
        : String.format("%s%s", type.toString().substring(0, 1).toUpperCase(), type.toString().substring(1));
  }

  /**
   * Extracts the value of api_key json attribute from the json arg
   * @param json json text
   * @param id the id of the resource the api_key belongs
   * @return api key
   * @throws JsonProcessingException in case json processing failed
   */
  public static String extractApiKey(String json, String id)
      throws JsonProcessingException {
    JsonNode node = Util.getNode(json, "created_roles", id, "api_key");
    return node.textValue();
  }

  /**
   * Returns an indented json string of the text argument.
   * @param text the json to indent
   * @return indented json string
   */
  public static String prettyPrintJson(String text) {
    ObjectMapper mapper = new ObjectMapper();
    Object json;
    String indented;

    try {
      json = mapper.readValue(text, Object.class);
      indented = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
    } catch (JsonProcessingException e) {
      /* ignore response might not be json*/
      return text;
    }

    return indented;
  }

  /**
   * Returns a JsonNode based on the path args
   * @param json the json to query
   * @param path path to the node
   * @return JsonNode based on the path args
   * @throws JsonProcessingException in case json processing failed
   */
  public static JsonNode getNode(String json, String... path) throws JsonProcessingException {
    ObjectMapper objMapper = new ObjectMapper();
    JsonNode node = objMapper.readTree(json);

    for (String p : path) {
      node = node.path(p);
    }

    return node;
  }

  /**
   * Returns false if the object is not null or the string representation of the object
   * is not an empty string.
   * @param obj the object to evaluate
   * @return boolean indication
   */
  public static boolean nonNullOrEmptyString(Object obj) {
    return obj != null && obj.toString().length() > 0;
  }

  /**
   * Returns true if the string arg is null or empty string.
   * @param string the string to evaluate
   * @return boolean indication
   */
  public static boolean isNullOrEmptyString(String string) {
    return string == null || string.length() == 0;
  }

  /**
   * Serializes the json input into the valueType arg.
   * @param json the objects data
   * @param valueType the destination classes
   * @param <T> type of class to serialize to.
   * @return Serialized class based on json input.
   * @throws JsonProcessingException in case serialization fails
   */
  public static <T> T readValue(String json, Class<T> valueType) throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    return mapper.readValue(json, valueType);
  }

  /**
   * Returns a view type of the resource type argument.
   * @param type resource type
   * @return view type
   */
  public static ViewType getViewType(ResourceType type) {
    switch(type) {
      case host:
        return ViewType.Hosts;
      case user:
        return ViewType.Users;
      case policy:
        return ViewType.Policies;
      case variable:
        return ViewType.Secrets;
      case layer:
        return ViewType.Layers;
      case group:
        return ViewType.Groups;
      case webservice:
        return ViewType.Webserivices;
      case host_factory:
        return ViewType.HostFactories;
    }

    throw new IllegalArgumentException(String.format("Resource type %s has no view", type));
  }

  /**
   * Returns a resource type of the view type argument.
   * @param view view type
   * @return resource type
   */
  public static ResourceType getResourceType(ViewType view) {
    switch(view) {
      case Hosts:
        return ResourceType.host;
      case Users:
        return ResourceType.user;
      case Policies:
        return ResourceType.policy;
      case Secrets:
        return ResourceType.variable;
      case Layers:
        return ResourceType.layer;
      case Groups:
        return ResourceType.group;
      case Webserivices:
        return ResourceType.webservice;
      case HostFactories:
        return ResourceType.host_factory;
    }

    throw new IllegalArgumentException();
  }

  /**
   * Masks the input as an array of '*'
   * @param secret the secret to mask
   * @return String array of '*' in the size of secret argument
   */
  public static String maskSecret(char[] secret) {
    char[] array = new char[secret !=  null ? secret.length : 0];
    Arrays.fill(array, '*');
    return new String(array);
  }

  /**
   * Returns true if the type argument is not of type variable or webservice
   * @param type the resource type to evaluate
   * @return boolean indication
   */
  public static boolean isRoleResource(ResourceType type) {
    return type == ResourceType.host ||
        type == ResourceType.user ||
        type == ResourceType.layer ||
        type == ResourceType.group;
  }

  /**
   * Returns true if the type argument is of type layer or group
   * @param type the resource type to evaluate
   * @return boolean indication
   */
  public static boolean isSetResource(ResourceType type) {
    return type == ResourceType.layer || type == ResourceType.group;
  }

  /**
   * Loads a Properties object with file from disk.
   * @return Properties object
   * @throws FileNotFoundException if the properties file is not found
   */
  public static InputStream getProperties(String name) throws FileNotFoundException {
    InputStream resourceAsStream = Util.class.getResourceAsStream(name);

    if (resourceAsStream == null) {
      throw new FileNotFoundException(name);
    }

    return resourceAsStream;
  }

  public static String generatePassword() {
    int length = 32;
    String uppers = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    String lowers = "abcdefghijklmnopqrstuvwxyz";
    String digits = "0123456789";
    String special = "!@@#$%^&*(){}[]?/`~\\|':;\"";

    StringBuilder password = null;
    StringBuilder charset = new StringBuilder();

    charset
        .append(uppers)
        .append(lowers)
        .append(digits)
        .append(special);
    boolean atLeastOneDigit = false;
    boolean atLeastOneSpecial = false;
    int uppersCount = 0;
    int lowersCount = 0;
    boolean allConditionsAreMet = false;

    while (!allConditionsAreMet) {
      password = new StringBuilder();

      for (int i = 0, n = charset.length(); i < length; ++i) {
        char ch = charset.charAt((int)Math.floor(Math.random() * n));

        if (Character.isLetter(ch)) {
          if (Character.isLowerCase(ch)) {
            lowersCount++;
          } else {
            uppersCount++;
          }
        } else if (Character.isDigit(ch)) {
          atLeastOneDigit = true;
        } else if (
            Arrays
                .stream(special.split(""))
                .anyMatch(s -> s.charAt(0) == ch)
        ) {
          atLeastOneSpecial = true;
        }

        password.append(ch);
      }

      allConditionsAreMet = atLeastOneDigit && atLeastOneSpecial && lowersCount >= 2 && uppersCount >= 2;

    }

    return password.toString();
  }

  public static Map<String, String> toMap(String json) {
    ObjectMapper mapper = new ObjectMapper();
    TypeReference<HashMap<String, String>> typeRef
        = new TypeReference<>() {};
    try {
      return mapper.readValue(json, typeRef);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException(e);
    }
  }

  public static String prettyDate(String time) {
    SimpleDateFormat formatter1 = new SimpleDateFormat(ISO_8601_24H_FULL_FORMAT);
    try {
      return (new PrettyTime()).format(formatter1.parse(time));
    } catch (ParseException e) {
      e.printStackTrace();
    }
    return null;
  }
}
