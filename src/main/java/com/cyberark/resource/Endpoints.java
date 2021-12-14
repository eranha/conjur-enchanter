package com.cyberark.resource;

public interface Endpoints {
  String LOGIN = "%s/authn/%s/login";
  String LOGOUT = "%s/authn/%s/logout";
  String AUTHN = "%s/authn/%s/%s/authenticate";
  String RESOURCES_KIND = "%s/resources/%s?kind=%s";
  String RESOURCES_KIND_COUNT = "%s/resources/%s?kind=%s&count=true";
  String RESOURCES = "%s/resources/%s";
  String SECRET = "%s/secrets/%s/variable/%s";
  String POLICY = "%s/policies/%s/policy/%s";
  String MEMBERSHIPS = "%s/roles/%s/%s/%s?memberships";
  String MEMBERS = "%s/roles/%s/%s/%s?members";
  String ROTATE_API_KEY = "%s/authn/%s/api_key?role=%s:%s";
  //String AUDIT = "%s/audit?limit=50";///audit/roles/org%3Auser%3Afoo?limit=10
  //  //api/audit/roles/org%3Auser%3Abob?limit=10
  String AUDIT = "%s/audit?limit=500";
}
