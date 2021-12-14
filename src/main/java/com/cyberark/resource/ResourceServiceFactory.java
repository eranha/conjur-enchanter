package com.cyberark.resource;

import com.cyberark.Application;
import com.cyberark.controllers.AccessTokenProvider;
import com.cyberark.controllers.AccessTokenProviderImpl;
import com.cyberark.event.EventPublisher;

public class ResourceServiceFactory {
  private static ResourceServiceFactory instance;
  private ResourcesService resourcesService;

  private ResourceServiceFactory() {
    EventPublisher.getInstance().addListener(e -> {
      if ("logout".equals(e.getActionCommand())) {
        setResourcesService(null);
      }
    });
  }

  public static ResourceServiceFactory getInstance() {
    if (instance == null) {
      instance = new ResourceServiceFactory();
    }
    return instance;
  }
  public ResourcesService getResourcesService() {
    if (resourcesService == null) {
      ResourceApiProvider apiProvider = new RestApiResourceProvider();
      setResourcesService(
          new ResourcesServiceImpl(
              apiProvider,
              new AccessTokenProviderImpl(apiProvider, Application.getInstance()::getCredentials)
          )
      );
    }
    return resourcesService;
  }

  public ResourcesService getResourcesService(ResourceApiProvider provider) {
    if (resourcesService == null) {
      setResourcesService(new ResourcesServiceImpl(provider,
          new AccessTokenProviderImpl(provider, Application.getInstance()::getCredentials)));
    }
    return resourcesService;
  }

  public ResourcesService getResourcesService(ResourceApiProvider resourceProvider,
                                              AccessTokenProvider accessTokenProvider) {
    if (resourcesService== null) {
      setResourcesService(new ResourcesServiceImpl(resourceProvider, accessTokenProvider));
    }
    return resourcesService;
  }

  public void setResourcesService(ResourcesService resourcesService) {
    this.resourcesService = resourcesService;
  }
}
