package com.cyberark.controllers;

public interface PolicyStatements {
  String RESTRICTED_TO = "  restricted_to: %s";
  String ANNOTATIONS = "  annotations:";
  String NAME_VALUE = "    %s: \"%s\"%n";
  String ID = "  id: %s%n";
  String TYPE = "- !%s%n";
  String GRANT = "- !grant\n" +
      "  role: !%s %s\n" +
      "  members:%n";
  String TYPE_ID = "    - !%s %s%n";
  String PERMIT = "- !permit%n" +
      "  resource: !%s %s%n" +
      "  privileges: [%s]%n" +
      "  role: !%s %s%n";

  String POLICY_WITH_OWNER = "- !policy%n" +
      "  id: %s%n" +
      "  owner: !%s %s%n" +
      "  body:";

  String POLICY = "- !policy%n" +
                  "  id: %s%n" +
                  "  body:";

  String RESOURCE = "- !%s %s";
  String OWNER =    "  owner: !%s %s%n";
}