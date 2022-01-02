package com.cyberark.resource;

import com.cyberark.Consts;
import com.cyberark.exceptions.ResourceAccessException;
import com.cyberark.models.*;
import com.cyberark.models.audit.AuditEvent;

import java.util.*;
import java.util.function.Predicate;

public interface ResourcesService extends Consts {
  void delete(ResourceModel resourceModel)
      throws ResourceAccessException;

  String getSecret(SecretModel model) throws ResourceAccessException;

  int getResourcesCount(ResourceType resourceType) throws ResourceAccessException;

  String setSecret(SecretModel model, String value) throws ResourceAccessException;

  List<ResourceModel> getResources(ResourceType type) throws ResourceAccessException;

  List<PolicyModel> getPolicies() throws ResourceAccessException;

  List<RoleModel> getRoles(ResourceType roleType) throws ResourceAccessException;

  List<SecretModel> getVariables() throws ResourceAccessException;

  String copyPermissions(ResourceModel model, String resourceId) throws ResourceAccessException;

  List<Membership> getMembership(ResourceIdentifier model) throws ResourceAccessException;

  String loadPolicy(String policyText) throws ResourceAccessException;

  String loadPolicy(PolicyApiMode policyApiMode, String policy, String branch) throws ResourceAccessException;

  String loadPolicy(String policy, String branch) throws ResourceAccessException;

  String rotateApiKey(ResourceType type, ResourceModel model) throws ResourceAccessException;

  String grant(RoleModel role, List<ResourceIdentifier> grantingRoles) throws ResourceAccessException;

  String revoke(RoleModel role, List<ResourceIdentifier> grantingRoles) throws ResourceAccessException;

  String grant(List<ResourceIdentifier> members, ResourceIdentifier role) throws ResourceAccessException;

  String revoke(List<ResourceIdentifier> members, ResourceIdentifier role) throws ResourceAccessException;

  String addRole(ResourceType type, RoleModel model, List<ResourceIdentifier> grantedSetRoles)
      throws ResourceAccessException;

  String addResource(ResourceType type, ResourceModel model) throws ResourceAccessException;

  String addResource(ResourceType type, ResourceModel model, List<ResourceIdentifier> members)
      throws ResourceAccessException;

  List<ResourceIdentifier> getResourceIdentifiers(Predicate<ResourceType> filter) throws ResourceAccessException;

  List<ResourceIdentifier> getResourceIdentifiers() throws ResourceAccessException;

  void deny(ResourceModel resource, HashMap<ResourceIdentifier, Set<String>> privileges)
      throws ResourceAccessException;

  /**
   * Grants the role argument with privileges to the resource argument.
   * @param role Identifies the privileged role
   * @param privileges Map of resource to privileges
   * @throws ResourceAccessException if any exception occurs.
   */
  void permit(ResourceModel role, Map<ResourceIdentifier, Set<String>> privileges)
      throws ResourceAccessException;

  /**
   * Grants all roles in privileges map argument with privileges to the resource argument.
   * @param resource Identifies the resource whose access is being controlled.
   * @param privileges Map of role to privileges
   * @throws ResourceAccessException if any exception occurs.
   */
  void permit(Map<ResourceIdentifier, Set<String>> privileges, ResourceModel resource)
      throws ResourceAccessException;

  /**
   * Returns a map of policy id to its flat list of resources.
   * @return a map of policy id to its flat list of resources.
   * @throws ResourceAccessException In case of an error.
   */
  Map<ResourceIdentifier, List<ResourceIdentifier>> getPolicyResources() throws ResourceAccessException;

  List<Membership> getMembers(ResourceIdentifier resource) throws ResourceAccessException;

  List<AuditEvent> getAuditEvents() throws ResourceAccessException;

  /**
   * Sets the password of the user argument to the password argument.
   * @param user The password owner user
   * @param password The new password
   * @param apiKey Required for authentication
   * @throws ResourceAccessException In case of operation failure
   */
  void updateUserPassword(RoleModel user, char[] password, char[] apiKey) throws ResourceAccessException;

  /**
   * Gets the API key of a role given the username and password via HTTP Basic Authentication
   * @param role The api key owner
   * @param password Thr password of the role
   * @return Securely generated random api key
   * @throws ResourceAccessException in case of error
   */
  String getApiKey(RoleModel role, char[] password) throws ResourceAccessException;

  /**
   * Immediately expires the secret value and rotates the existing secret value of the secret
   * corresponding to the secretModel argument.
   * @param secretModel The secret to rotate
   * @throws ResourceAccessException If the operation fails
   */
  void rotateSecret(SecretModel secretModel) throws ResourceAccessException;

  List<HostFactory> getHostFactories() throws ResourceAccessException;

  String createHostFactoryTokens(HostFactoryTokensFormModel model) throws ResourceAccessException;

  String createHostFactoryHost(String hostName, String token) throws ResourceAccessException;

  void revokeHostFactoryToken(String token) throws ResourceAccessException;

  String addHostFactory(HostFactory model)  throws ResourceAccessException;
}
