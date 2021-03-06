package com.cyberark.resource;

import com.cyberark.Application;
import com.cyberark.Credentials;
import com.cyberark.PolicyBuilder;
import com.cyberark.Util;
import com.cyberark.controllers.AccessTokenProvider;
import com.cyberark.exceptions.AuthenticationException;
import com.cyberark.exceptions.ResourceAccessException;
import com.cyberark.models.*;
import com.cyberark.models.audit.AuditEvent;
import com.cyberark.models.audit.AuditEventSubject;
import com.cyberark.models.hostfactory.HostFactory;
import com.cyberark.models.hostfactory.HostFactoryHostModel;
import com.cyberark.models.hostfactory.HostFactoryTokensFormModel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.CoercionAction;
import com.fasterxml.jackson.databind.cfg.CoercionInputShape;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.cyberark.Util.readValue;

class ResourcesServiceImpl implements ResourcesService {
  public static final String ROOT_POLICY = "root";
  private final Application app = Application.getInstance();
  private final ResourceApiProvider resourceProvider;
  private final AccessTokenProvider accessTokenProvider;
  private static final Logger logger = LogManager.getLogger(ResourcesServiceImpl.class);

  ResourcesServiceImpl(
      ResourceApiProvider resourceProvider,
      AccessTokenProvider accessTokenProvider) {
    this.resourceProvider = resourceProvider;
    this.accessTokenProvider = accessTokenProvider;
  }

  @Override
  public void delete(ResourceModel model)
      throws ResourceAccessException {

    validateResourceModel(model);

    if (Util.isNullOrEmptyString(model.getPolicy())) {
      throw new IllegalArgumentException("Resource policy is missing");
    }

    PolicyBuilder policyBuilder = new PolicyBuilder();

    policyBuilder.delete(model.getIdentifier());
    String policy = ResourceIdentifier.fromString(model.getPolicy()).getId();

    patchPolicy(policyBuilder.toPolicy(), policy);
  }

  @Override
  public String getSecret(SecretModel model) throws ResourceAccessException {
    validateResourceModel(model);
    String id = getResourceId(model).getId();

    String url = String.format(
        Endpoints.SECRET,
        getCredentials().url,
        getCredentials().account,
        id);

    try {
      return resourceProvider.get(new URL(url), getAccessToken());
    } catch (IOException e) {
      e.printStackTrace();
      throw new ResourceAccessException(e);
    }
  }

  @Override
  public int getResourcesCount(ResourceType resourceType) throws ResourceAccessException {
    try {
      String json = resourceProvider.get(
          new URL(getResourcesEndpoint(Endpoints.RESOURCES_KIND_COUNT, resourceType)), getAccessToken()
      );

      return (int) readValue(json, Map.class).get("count");
    } catch (IOException e) {
      e.printStackTrace();
      throw new ResourceAccessException(e);
    }
  }

  @Override
  public String setSecret(SecretModel model, String value) throws ResourceAccessException {
    validateResourceModel(model);
    String id = getResourceId(model).getId();

    if (value == null) {
      throw new IllegalArgumentException("value");
    }

    String url = String.format(
        Endpoints.SECRET,
        app.getCredentials().url,
        app.getCredentials().account,
        id);
    try {
      return resourceProvider.post(new URL(url), getAccessToken(), value);
    } catch (IOException e) {
      e.printStackTrace();
      throw new ResourceAccessException(e);
    }
  }

  @Override
  public void rotateSecret(SecretModel model) throws ResourceAccessException {
    validateResourceModel(model);
    String id = getResourceId(model).getId();


    String url = String.format(
        Endpoints.ROTATE_SECRET,
        app.getCredentials().url,
        app.getCredentials().account,
        id);
    try {
      resourceProvider.post(new URL(url), getAccessToken(), null);
    } catch (IOException e) {
      e.printStackTrace();
      throw new ResourceAccessException(e);
    }
  }

  @Override
  public List<HostFactory> getHostFactories() throws ResourceAccessException {
    try {
      String json = resourceProvider.get(new URL(getResourcesEndpoint(ResourceType.host_factory)), getAccessToken());
      return Arrays.stream(readValue(json, HostFactory[].class))
          .sorted((Comparator.comparing(ResourceModel::getId)))
          .collect(Collectors.toList());
    } catch (IOException e) {
      e.printStackTrace();
      throw new ResourceAccessException(e);
    }
  }

  @Override
  public String createHostFactoryTokens(HostFactoryTokensFormModel model) throws ResourceAccessException {
    if (model == null) {
      throw new IllegalArgumentException("model");
    }

    logger.trace("createHostFactoryTokens({}) enter", model.getHostFactoryId());
    StringBuilder builder = new StringBuilder();

    builder
        .append("expiration=").append(
        URLEncoder.encode(model.getExpirationUtcCDate().replace(' ', 'T'),
            StandardCharsets.UTF_8))
        .append(URLEncoder.encode("+00:00", StandardCharsets.UTF_8))
        .append("&count=").append(model.getNumberOfTokens())
        .append ("&host_factory=").append(URLEncoder.encode(model.getHostFactoryId(), StandardCharsets.UTF_8));

    if (Objects.nonNull(model.getRestrictions())) {
      for (String ip : model.getRestrictions()) {
        builder.append("&cidr[]=").append(URLEncoder.encode(ip, StandardCharsets.UTF_8));
      }
    }

    String payload = builder.toString();

    String response;
    HashMap<String, String> headers = new HashMap<>();
    headers.put("Authorization", String.format("Token token=\"%s\"",
        Base64.getEncoder().encodeToString(new String(getAccessToken()).getBytes())));
    headers.put("Content-Type", "application/x-www-form-urlencoded");//charset
    headers.put("Charset", StandardCharsets.UTF_8.toString());

    try {
      String url = String.format(
          Endpoints.HOST_FACTORY_TOKENS,
          app.getCredentials().url);

      response = resourceProvider.post(
        new URL(url),
        headers,
        payload
      );
    } catch (IOException e) {
      e.printStackTrace();
      logger.error(e);
      throw new ResourceAccessException(e);
    }
    logger.trace("createHostFactoryTokens({}) exit", model.getHostFactoryId());
    return response;
  }

  @Override
  public String createHostFactoryHost(HostFactoryHostModel model) throws ResourceAccessException {
    logger.trace("createHostFactoryHost({}) enter", model);
    String response;

    try {
      String url = String.format(
          Endpoints.HOST_FACTORY_HOSTS,
          app.getCredentials().url);
      HashMap<String, String> headers = new HashMap<>();
      headers.put("Authorization", String.format("Token token=\"%s\"", model.getHostFactoryToken().getToken()));
      StringBuilder payload = new StringBuilder();
      payload.append(String.format("id=%s", model.getHostName()));
      if (Objects.nonNull(model.getAnnotations()) && model.getAnnotations().length > 0) {
        payload.append("&");
        Arrays.stream(model.getAnnotations()).forEach(
          a -> payload.append(
              String.format("annotations[%s]=%s&",
                URLEncoder.encode(a.getName(), StandardCharsets.UTF_8),
                URLEncoder.encode(a.getValue(), StandardCharsets.UTF_8)
              )
             )
        );
      }

      response = resourceProvider.post(new URL(url), headers, payload.toString());

    } catch (IOException e) {
      e.printStackTrace();
      logger.error(e);
      throw new ResourceAccessException(e);
    }
    logger.trace("createHostFactoryHost({}) exit", model.getHostName());

    return response;
  }

  @Override
  public void revokeHostFactoryToken(String token) throws ResourceAccessException {
    logger.trace("revokeHostFactoryToken({}) enter", token);

    if (token == null) {
        throw new IllegalArgumentException("token");
    }

    try {
      String url = String.format(
          Endpoints.DELETE_HOST_FACTORY_TOKENS,
          app.getCredentials().url, token);

      resourceProvider.request(new URL(url), "DELETE", getAccessToken(), null);
    } catch (IOException e) {
      e.printStackTrace();
      logger.error(e);
      throw new ResourceAccessException(e);
    }
    logger.trace("revokeHostFactoryToken({}) exit", token);
  }

  @Override
  public String addHostFactory(HostFactory model) throws ResourceAccessException {
    logger.trace("addHostFactory({}) enter", model);

    String response;

    if (model == null) {
      throw new IllegalArgumentException("model");
    }

    PolicyBuilder policyBuilder = new PolicyBuilder();
    String policy = policyBuilder
        .resource(model.getIdentifier())//, ResourceIdentifier.fromString(model.getOwner()))
        .layers(model.getLayers())
        .annotations(model.getAnnotations())
        .toPolicy();

    response = loadPolicy(policy, Objects.isNull(model.getPolicy())
        ? ROOT_POLICY
        : ResourceIdentifier.fromString(model.getPolicy()).getId());

    logger.trace("addHostFactory({}) exit return: {}", model, response);

    return response;
  }

  @Override
  public List<ResourceModel> getResources(ResourceType type) throws ResourceAccessException {
    logger.trace("getResources({}) enter", type);

    if (type == null) {
      throw new IllegalArgumentException("type");
    }

    List<ResourceModel> resources;

    try {
      String data = resourceProvider.get(new URL(getResourcesEndpoint(type)), getAccessToken());

      // sort by id
      resources = Arrays.stream(readValue(data, ResourceModel[].class))
          .sorted((Comparator.comparing(ResourceModel::getId)))
          .collect(Collectors.toList());
    } catch (IOException e) {
      e.printStackTrace();
      logger.error(e);
      throw new ResourceAccessException(e);
    }

    logger.trace("getResources({}) exit return {} item(s)", type,
        resources.size());

    return resources;
  }


  @Override
  public List<PolicyModel> getPolicies() throws ResourceAccessException {
    try {
      String json = resourceProvider.get(new URL(getResourcesEndpoint(ResourceType.policy)), getAccessToken());
      return Arrays.stream(readValue(json, PolicyModel[].class))
          .sorted((Comparator.comparing(ResourceModel::getId)))
          .collect(Collectors.toList());
    } catch (IOException e) {
      e.printStackTrace();
      throw new ResourceAccessException(e);
    }
  }

  @Override
  public List<RoleModel> getRoles(ResourceType roleType) throws ResourceAccessException {
    if (roleType == null) {
      throw  new IllegalArgumentException("roleType");
    }

    if (roleType != ResourceType.user && roleType != ResourceType.host) {
      throw  new IllegalArgumentException("roleType");
    }

    try {
      String json = resourceProvider.get(new URL(getResourcesEndpoint(roleType)), getAccessToken());
      return Arrays.stream(readValue(json, RoleModel[].class))
          .sorted((Comparator.comparing(ResourceModel::getId)))
          .collect(Collectors.toList());
    } catch (IOException e) {
      e.printStackTrace();
      throw new ResourceAccessException(e);
    }
  }

  @Override
  public List<SecretModel> getVariables() throws ResourceAccessException {
    try {
      String json = resourceProvider.get(new URL(getResourcesEndpoint(ResourceType.variable)), getAccessToken());
      return Arrays.stream(readValue(json, SecretModel[].class))
          .sorted((Comparator.comparing(ResourceModel::getId)))
          .collect(Collectors.toList());
    } catch (IOException e) {
      e.printStackTrace();
      throw new ResourceAccessException(e);
    }
  }

  @Override
  public String copyPermissions(ResourceModel model, String resourceId) throws ResourceAccessException {
    validateResourceModel(model);

    if (Util.isNullOrEmptyString(resourceId)) {
      throw new IllegalArgumentException("resourceId");
    }

    PolicyBuilder policyBuilder = new PolicyBuilder();

    policyBuilder.permissions(
        ResourceIdentifier.fromString(
          String.format("%s:%s:%s", app.getCredentials().account, model.getIdentifier().getType(), resourceId)
        ),
        model.getPermissions()
    );

    return patchPolicy(policyBuilder.toPolicy(), ROOT_POLICY);
  }

  @Override
  public List<Membership> getMembership(ResourceIdentifier model) throws ResourceAccessException {
    validateResourceIdentifier(model);

    String url = String.format(
        Endpoints.MEMBERSHIPS,
        getCredentials().url,
        getCredentials().account,
        model.getType(),
        model.getId());

    try {
      String json = resourceProvider.get(new URL(url), getAccessToken());
      return Arrays.stream(readValue(json, Membership[].class)).collect(Collectors.toList());
    } catch (IOException e) {
      e.printStackTrace();
      throw new ResourceAccessException(e);
    }
  }

  @Override
  public String loadPolicy(String policyText) throws ResourceAccessException {
    return loadPolicy(policyText, ROOT_POLICY);
  }

  @Override
  public String loadPolicy(String policy, String branch) throws ResourceAccessException {
    return loadPolicy(PolicyApiMode.Post, policy, branch);
  }

  @Override
  public String loadPolicy(PolicyApiMode policyApiMode, String policy, String branch) throws ResourceAccessException {
    branch = branch == null ? ROOT_POLICY : branch;

    if (Util.isNullOrEmptyString(policy)) {
      throw new IllegalArgumentException("policy");
    }

    if (Util.isNullOrEmptyString(branch)) {
      throw new IllegalArgumentException("branch");
    }

    logger.debug("\n{}", policy);

    try {
      URL url = new URL(
          String.format(Endpoints.POLICY,
              getCredentials().url,
              getCredentials().account,
              branch
          )
      );

      switch (policyApiMode) {
        case Post:
          return resourceProvider.post(
              url,
              getAccessToken(),
              policy
          );
        case Put:
          return resourceProvider.request(
              url,
              "PUT",
              getAccessToken(),
              policy
          );
        case Patch:
          return patchPolicy(
              policy,
              branch
          );
      }
    } catch (IOException e) {
      e.printStackTrace();
      throw new ResourceAccessException(e);
    }

    throw new IllegalStateException();
  }

  @Override
  public String rotateApiKey(ResourceType type, ResourceModel model) throws ResourceAccessException {
    validateResourceModel(model);

    if (type == null) {
      throw new IllegalArgumentException("type");
    }

    String id = URLEncoder.encode(getResourceId(model).getId(), StandardCharsets.UTF_8);
    String url = String.format(
        Endpoints.ROTATE_API_KEY,
        getCredentials().url,
        getCredentials().account,
        type,
        id);

    try {
      return resourceProvider.request(new URL(url), "PUT", getAccessToken(), null);
    } catch (IOException e) {
      e.printStackTrace();
      throw new ResourceAccessException(e);
    }
  }

  @Override
  public String grant(RoleModel role, List<ResourceIdentifier> resources) throws ResourceAccessException {
    validateResourceModel(role);

    if (resources == null || resources.isEmpty()) {
      throw new IllegalArgumentException("resources");
    }

    Predicate<ResourceIdentifier> invalidResourceType = r ->
        r.getType() != ResourceType.group && r.getType() != ResourceType.layer;

    if (resources.stream().anyMatch(invalidResourceType)) {
      throw new ResourceAccessException(
          new IllegalArgumentException("kind-of-role must be either group or layer"));
    }

    PolicyBuilder policyBuilder = new PolicyBuilder();
    policyBuilder.grants(resources, role.getIdentifier());
    return loadPolicy(policyBuilder.toPolicy());
  }

  @Override
  public String grant(List<ResourceIdentifier> resources, ResourceIdentifier role) throws ResourceAccessException {
    validateResourceIdentifier(role);

    if (role.getType() != ResourceType.group && role.getType() != ResourceType.layer) {
      throw new ResourceAccessException(
          new IllegalArgumentException("kind-of-role must be either group or layer"));
    }

    if (resources == null || resources.isEmpty()) {
      throw new IllegalArgumentException("resources");
    }

    if (resources.stream().anyMatch(r -> r.getFullyQualifiedId().equals(role.getFullyQualifiedId()))) {
      throw new ResourceAccessException(
          new IllegalArgumentException("Cannot add resource to itself"));
    }

    Predicate<ResourceIdentifier> invalidResourceType = r ->
        r.getType() == ResourceType.policy || r.getType() == ResourceType.webservice;

    if (resources.stream().anyMatch(invalidResourceType)) {
      throw new ResourceAccessException(
          new IllegalArgumentException("List may only contain either group, layer, user, host resources"));
    }

    PolicyBuilder policyBuilder = new PolicyBuilder();
    policyBuilder.grants(role, resources);

    return loadPolicy(policyBuilder.toPolicy());
  }

  @Override
  public String revoke(RoleModel role, List<ResourceIdentifier> roles) throws ResourceAccessException {
    validateResourceModel(role);

    if (roles == null || roles.isEmpty()) {
      throw new IllegalArgumentException("resources");
    }

    Predicate<ResourceIdentifier> invalidResourceType = r ->
        r.getType() != ResourceType.group  && r.getType() != ResourceType.layer;

    if (roles
        .stream()
        .anyMatch(invalidResourceType)) {
      throw new ResourceAccessException(
          new IllegalArgumentException("list may only contain either group or layer resources"));
    }

    ResourceIdentifier member = role.getIdentifier();
    PolicyBuilder policyBuilder = new PolicyBuilder();
    policyBuilder.revoke(member, roles);

    return patchPolicy(policyBuilder.toPolicy(), ROOT_POLICY);
  }

  @Override
  public String revoke(List<ResourceIdentifier> members, ResourceIdentifier role) throws ResourceAccessException {
    validateResourceIdentifier(role);

    if (members == null || members.isEmpty()) {
      throw new IllegalArgumentException("members");
    }

    if (role.getType() != ResourceType.group && role.getType() != ResourceType.layer) {
      throw new ResourceAccessException(
          new IllegalArgumentException("kind-of-role must be either group or layer"));
    }


    Predicate<ResourceIdentifier> invalidResourceType = r ->
        r.getType() == ResourceType.policy || r.getType() == ResourceType.webservice;

    if (members
        .stream()
        .anyMatch(invalidResourceType)) {
      throw new ResourceAccessException(
          new IllegalArgumentException("List may only contain either group, layer, user, host members"));
    }

    PolicyBuilder policyBuilder = new PolicyBuilder();
    policyBuilder.revoke(members, role);

    return patchPolicy(policyBuilder.toPolicy(), ROOT_POLICY);
  }

  private String patchPolicy(String policy, String branch) throws ResourceAccessException {
    logger.debug("\n{}", policy);

    try {
      return resourceProvider.request(
          new URL(String.format(Endpoints.POLICY, app.getCredentials().url, app.getCredentials().account, branch)),
          "PATCH",
          getAccessToken(),
          policy);
    } catch (IOException e) {
      throw new ResourceAccessException(e);
    }
  }

  @Override
  public String addRole(ResourceType type,
                        RoleModel model,
                        List<ResourceIdentifier> grantedSetRoles)
    throws ResourceAccessException {
    logger.trace("enter:: addRole(type={}, model={}, grantedSetRoles{})", type, model, grantedSetRoles);
    validateResourceModel(model);

    if (type == null) {
      throw new IllegalArgumentException("type");
    }

    PolicyBuilder policyBuilder = new PolicyBuilder();
    policyBuilder
        .resource(model.getIdentifier())
        .annotations(model.getAnnotations())
        .restrictions(model.getRestrictedTo());

    String response = loadPolicy(policyBuilder.toPolicy(), model.getPolicy());

    // Create grants in root policy
    if (!grantedSetRoles.isEmpty()) {
      policyBuilder = new PolicyBuilder();
      loadPolicy(policyBuilder.grants(grantedSetRoles, model.getIdentifier()).toPolicy(), ROOT_POLICY);
    }

    logger.trace("exit:: return: {}", response);

    return response;
  }

  @Override
  public String addResource(ResourceType type, ResourceModel model)
      throws ResourceAccessException {
    return addResource(type, model, new ArrayList<>());
  }

  public String addResource(ResourceType type, ResourceModel model, List<ResourceIdentifier> members)
      throws ResourceAccessException {
    logger.trace("enter::addResource({}, {}, {})", type, model, members);
    validateResourceModel(model);

    if (type == null) {
      throw new IllegalArgumentException("type");
    }

    StringBuilder policy = buildPolicyString(type, model, members);

    logger.debug("Policy:\n{}", policy);
    String response = loadPolicy(policy.toString(), model.getPolicy() != null ? model.getPolicy() : ROOT_POLICY);
    logger.trace("exit::addResource:: response: {}", response);
    return response;
  }

  private StringBuilder buildPolicyString(
      ResourceType type,
      ResourceModel model,
      List<ResourceIdentifier> members) {
    StringBuilder policy = new StringBuilder();
    policy.append(String.format("- !%s\n", type));
    policy.append(String.format("  id: %s\n", model.getIdentifier().getId()));

    if (model.getOwner() != null) {
      ResourceIdentifier owner = ResourceIdentifier.fromString(model.getOwner());
      policy.append(String.format("  owner: !%s %s\n", owner.getType(), owner.getId()));
    }

    if (model.getAnnotations().length > 0) {
      policy.append("  annotations:\n");

      for (Annotation annotation : model.getAnnotations()) {
        policy.append(String.format("    %s: \"%s\"\n", annotation.getName(), annotation.getValue()));
      }
    }

    if (members.size() > 0) {
      policy.append("- !grant").append(System.lineSeparator());
      policy.append(String.format("  role: !%s %s%n", type, model.getIdentifier().getId()));
      policy.append("  members:").append(System.lineSeparator());

      for (ResourceIdentifier i : members) {
        policy.append(String.format("    - !%s %s%n", i.getType(), i.getId()));
      }
    }

    return policy;
  }

  @Override
  public void deny(ResourceModel resource, HashMap<ResourceIdentifier, Set<String>> roleToPrivileges)
      throws ResourceAccessException {
    logger.trace("enter::deny(resource={}, roleToPrivileges={})", resource, roleToPrivileges);

    PolicyBuilder policyBuilder = new PolicyBuilder();

    roleToPrivileges.forEach(
        (role, privileges) -> policyBuilder.deny(role, resource.getIdentifier(), privileges)
    );

    patchPolicy(policyBuilder.toPolicy(), resource.getPolicy() == null
        ? ROOT_POLICY
        : ResourceIdentifier.fromString(resource.getPolicy()).getId());
    logger.trace("exit::deny(resource={}, roleToPrivileges={})", resource, roleToPrivileges);
  }

  @Override
  public void permit(ResourceModel role, Map<ResourceIdentifier, Set<String>> privileges)
      throws ResourceAccessException {
    logger.trace("enter::permit(resource={}, roleToPrivileges={})", role, privileges);

    PolicyBuilder policyBuilder = new PolicyBuilder();

    privileges.forEach(
        (r, p) -> policyBuilder.permit(role.getIdentifier(), r, p)
    );

    String policy = policyBuilder.toPolicy();
    loadPolicy(policy, ROOT_POLICY);

    logger.trace("exit::permit(resource={}, roleToPrivileges={})", role, privileges);
  }

  @Override
  public void permit(Map<ResourceIdentifier, Set<String>> privileges, ResourceModel resource)
      throws ResourceAccessException {
    logger.trace("enter::permit(privileges={}, resource={})", privileges, resource);

    PolicyBuilder policyBuilder = new PolicyBuilder();

    privileges.forEach((role, p) ->
      policyBuilder.permit(role, resource.getIdentifier(), p)
    );

    String policy = policyBuilder.toPolicy();
    loadPolicy(policy, ROOT_POLICY);

    logger.trace("exit::permit(privileges={}, resource={})", privileges, resource);
  }

  @Override
  public Map<ResourceIdentifier, List<ResourceIdentifier>> getPolicyResources() throws ResourceAccessException {
    HashMap<ResourceIdentifier, List<ResourceIdentifier>> map = new HashMap<>();
    String data = get(String.format(Endpoints.RESOURCES, app.getCredentials().url, app.getCredentials().account));
    ObjectMapper objMapper = new ObjectMapper();

    try {
      JsonNode node = objMapper.readTree(data);
      if (node.isArray()) {
        StreamSupport.stream(node.spliterator(), false /* or whatever */)
            .forEach(n ->
                {
                  ResourceIdentifier id = ResourceIdentifier.fromString(n.get("id").textValue());
                  if (id.getType() == ResourceType.policy) {
                    map.putIfAbsent(ResourceIdentifier.fromString(n.get("id").textValue()), new ArrayList<>());
                  } else {
                    final JsonNode policy = n.get("policy");
                    List<ResourceIdentifier> resources = null;
                    if (Objects.nonNull(policy)) { // host_factory and host_factory/host have no policy
                      resources = map.get(ResourceIdentifier.fromString(policy.textValue()));
                      resources.add(id);
                    }
                  }
                }
            );
      }
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      throw new ResourceAccessException(e);
    }
    return map;
  }

  @Override
  public List<Membership> getMembers(ResourceIdentifier resource) throws ResourceAccessException {
    validateResourceIdentifier(resource);

    String url = String.format(
        Endpoints.MEMBERS,
        getCredentials().url,
        getCredentials().account,
        resource.getType(),
        resource.getId());

    try {
      String json = resourceProvider.get(new URL(url), getAccessToken());
      return Arrays.stream(readValue(json, Membership[].class)).collect(Collectors.toList());
    } catch (IOException e) {
      e.printStackTrace();
      throw new ResourceAccessException(e);
    }
  }

  @Override
  public List<AuditEvent> getAuditEvents() throws ResourceAccessException {
    AuditEvent[] auditModels;

    try {
      String json = resourceProvider.get(new URL(String.format(Endpoints.AUDIT, getCredentials().url)),
          getAccessToken());

      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.coercionConfigFor(AuditEventSubject.class)
          .setCoercion(CoercionInputShape.EmptyString, CoercionAction.AsNull);
      auditModels = objectMapper.readValue(json, AuditEvent[].class);
    } catch (IOException e) {
      e.printStackTrace();
      throw new ResourceAccessException(e);
    }

    return Arrays.stream(auditModels).collect(Collectors.toList());
  }

  @Override
  public void updateUserPassword(RoleModel user, char[] password, char[] apiKey) throws ResourceAccessException {
    validateResourceModel(user);

    if (user.getIdentifier().getType() != ResourceType.user) {
      throw new IllegalArgumentException("user");
    }

    if (password == null || password.length == 0) {
      throw new IllegalArgumentException("password");
    }

    if (apiKey == null || apiKey.length == 0) {
      throw new IllegalArgumentException("apiKey");
    }

    String url = String.format(
        Endpoints.UPDATE_PASSWORD,
        getCredentials().url,
        getCredentials().account);

    try {
      resourceProvider.put(new URL(url), user.getIdentifier().getId(), apiKey, new String(password));
    } catch (IOException e) {
      e.printStackTrace();
      throw new ResourceAccessException(e);
    }
  }

  @Override
  public String getApiKey(RoleModel role, char[] password) throws ResourceAccessException {
    validateResourceModel(role);

    if (password == null || password.length == 0) {
      throw new IllegalArgumentException("password");
    }

    String url = String.format(
        Endpoints.LOGIN,
        getCredentials().url,
        getCredentials().account);

    try {
      return resourceProvider.get(new URL(url), role.getIdentifier().getId(), password);
    } catch (IOException e) {
      e.printStackTrace();
      throw new ResourceAccessException(e);
    }
  }

  @Override
  public List<ResourceIdentifier> getResourceIdentifiers()
      throws ResourceAccessException {
    return getResourceIdentifiers(t -> true);
  }

  @Override
  public List<ResourceIdentifier> getResourceIdentifiers(Predicate<ResourceType> filter)
      throws ResourceAccessException {
    String data = get(String.format(Endpoints.RESOURCES, app.getCredentials().url, app.getCredentials().account));
    ObjectMapper objMapper = new ObjectMapper();
    List<ResourceIdentifier> resources = new ArrayList<>();

    try {
      JsonNode node = objMapper.readTree(data);
      if (node.isArray()) {
        resources = StreamSupport.stream(node.spliterator(), false /* or whatever */)
            .map(n -> ResourceIdentifier.fromString(n.get("id").textValue()))
            .filter(i -> filter.test(i.getType()))
            .collect(Collectors.toList());
      }
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      throw new ResourceAccessException(e);
    }
    return resources;
  }



  private ResourceIdentifier getResourceId(ResourceModel model) {
    ResourceIdentifier id = ResourceIdentifier.fromString(model.getId());

    if (Util.isNullOrEmptyString(id.getId())) {
      throw new IllegalArgumentException("Resource id is missing");
    }
    return id;
  }

  private void validateResourceModel(ResourceModel model) {
    if (model == null) {
      throw new IllegalArgumentException("model");
    }

    if (Util.isNullOrEmptyString(model.getId())) {
      throw new IllegalArgumentException("Resource id is missing");
    }
  }

  private void validateResourceIdentifier(ResourceIdentifier model) {
    if (model == null) {
      throw new IllegalArgumentException("model");
    }
  }

  private Credentials getCredentials() {
    return  Application.getInstance().getCredentials();
  }

  private String getResourcesEndpoint(ResourceType type) {
    return getResourcesEndpoint(Endpoints.RESOURCES_KIND, type);
  }

  private String getResourcesEndpoint(String endPointPattern, ResourceType type) {
    return String.format(endPointPattern,
        app.getCredentials().url, app.getCredentials().account, type);
  }



  private String get(String url) throws ResourceAccessException {
    try {
      return resourceProvider.get(new URL(url), getAccessToken());
    } catch (IOException e) {
      e.printStackTrace();
      throw new ResourceAccessException(e);
    }
  }

  private char[] getAccessToken() throws ResourceAccessException {
    try {
      return accessTokenProvider.getAccessToken();
    } catch (AuthenticationException e) {
      throw new ResourceAccessException(e);
    }
  }
}
