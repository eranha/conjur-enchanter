package com.cyberark.actions;

import com.cyberark.PolicyBuilder;
import com.cyberark.Util;
import com.cyberark.models.*;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class ResourceUtil {
  static String getResourcePolicy(ResourceModel resource) {
    String policy = null;
    PolicyBuilder policyBuilder = new PolicyBuilder();

    if (resource.getIdentifier().getType() == ResourceType.policy) {
      PolicyModel model = (PolicyModel) resource;

      if (model.getPolicyVersions().length > 0) {
        // get the latest policy version
        Optional<PolicyVersion> policyVersion = Arrays.stream(model.getPolicyVersions())
            .max(Comparator.comparingInt(PolicyVersion::getVersion))
            .stream()
            .findFirst();
        policy = policyVersion.map(PolicyVersion::getPolicyText).orElse(null);
      }
    } else {
      policy = policyBuilder.resource(resource.getIdentifier())
          .annotations(resource.getAnnotations())
          .permissions(resource.getIdentifier(), resource.getPermissions())
          .toPolicy();
    }

    return policy;
  }

  static String getResourcePermissions(ResourceIdentifier resource,
                                       List<ResourceIdentifier> memberRoles,
                                       List<ResourceIdentifier> grantedRoles) {
    PolicyBuilder policyBuilder = new PolicyBuilder();

    if (Util.isSetResource(resource.getType())) {
      // list members in a single grant
      policyBuilder.grants(resource, memberRoles);
    } else {
      // for each role this resource is granted, create a single grant
      policyBuilder
          .grants(grantedRoles, resource);
    }

    return policyBuilder.toPolicy();
  }
}
