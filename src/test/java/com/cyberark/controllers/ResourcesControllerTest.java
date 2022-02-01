package com.cyberark.controllers;

import com.cyberark.Application;
import com.cyberark.Credentials;
import com.cyberark.exceptions.ResourceAccessException;
import com.cyberark.models.PolicyModel;
import com.cyberark.resource.ResourcesService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ResourcesControllerTest {
//  ResourcesService resourcesService;
  //ResourceProviderMock resourceProviderMock = new ResourceProviderMock();

  @BeforeEach
  void setUp() {
    Application.getInstance().setCredentials(new Credentials("http://conjur", "test", "test_user",
        null));
//    resourcesService = ResourceServiceFactory.getInstance().getResourcesService(
//        resourceProviderMock, new AccessTokenProvider() {
//          @Override
//          public char[] getAccessToken() throws AuthenticationException {
//            return "access_token".toCharArray();
//          }
//        }
//    );
  }

  @AfterEach
  void tearDown() {
  }

  @Test
  void deleteResource() {
  }

  @Test
  void getSecret() {
  }

  @Test
  void setSecret() {
  }

  @Test
  void getResources() {
  }

  @Test
  void getPolicies() throws ResourceAccessException, JsonProcessingException {
    String policy = "- !policy\n" +
        "   id: p1 ";
    String returnValue = "{\n" +
        "  \"created_roles\" : { },\n" +
        "  \"version\" : 1\n" +
        "}";

    ResourcesService mock = mock(ResourcesService.class);
    when(mock.loadPolicy(policy)).thenReturn(returnValue);
    final String response = mock.loadPolicy(policy);

    String policyJson = "[    {\n" +
        "        \"created_at\": \"2021-11-23T13:41:59.103+00:00\",\n" +
        "        \"id\": \"org:policy:p1\",\n" +
        "        \"owner\": \"org:user:admin\",\n" +
        "        \"policy\": \"org:policy:root\",\n" +
        "        \"permissions\": [],\n" +
        "        \"annotations\": [],\n" +
        "        \"policy_versions\": []\n" +
        "    }]";
    List<PolicyModel> list = new ArrayList<>();
    list.add((new JsonMapper()).readValue(policyJson, PolicyModel[].class)[0]);
    when(mock.getPolicies()).thenReturn(list);
    final List<PolicyModel> policies1 = mock.getPolicies();
    System.out.println(policies1);
    //    List<PolicyModel> policies = resourcesService.getPolicies();
//    Assertions.assertEquals(1, policies.size());
//    Assertions.assertEquals("org:policy:p1", policies.get(0).id);
//    Assertions.assertEquals("org:user:admin", policies.get(0).owner);
//    Assertions.assertEquals("org:policy:root", policies.get(0).policy);
//    Assertions.assertEquals(0, policies.get(0).permissions.length);
//    Assertions.assertEquals(0, policies.get(0).annotations.length);
//    Assertions.assertEquals(0, policies.get(0).policy_versions.length);
  }

  @Test
  void getMembership() {
  }

  @Test
  void loadPolicy() {
//    String returnValue = "{\n" +
//        "  \"created_roles\" : { },\n" +
//        "  \"version\" : 1\n" +
//        "}";
//    resourceProviderMock.setReturnValue(returnValue);
//    String response = resourcesService.loadPolicy("- !policy\n" +
//        "   id: p1 ");
//    Assertions.assertEquals(returnValue, response);
  }

  @Test
  void testLoadPolicy() {
  }

  @Test
  void rotateApiKey() {
  }

  @Test
  void grant() {
  }

  @Test
  void revoke() {
  }

  @Test
  void addRole() {
  }

  @Test
  void addResource() {
  }

  @Test
  void getResourceIdentifiers() {
  }

  @Test
  void deny() {
  }

  @Test
  void permit() {
  }

  @Test
  void testGetResourceIdentifiers() {
  }

  @Test
  void getPolicyResources() {
  }
}