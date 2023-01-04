package com.k_int.sierra.keycloak.provider;

import org.keycloak.component.ComponentModel;
import org.keycloak.component.ComponentValidationException;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.storage.UserStorageProviderFactory;
import org.keycloak.utils.StringUtil;
import java.util.List;
import org.jboss.logging.Logger;
import org.keycloak.Config;

public class SierraUserStorageProviderFactory implements UserStorageProviderFactory<SierraUserStorageProvider> {

  public static final String PROVIDER_ID = "sierra-user";
  private static final Logger log = Logger.getLogger(SierraUserStorageProviderFactory.class);

  @Override
  public SierraUserStorageProvider create(KeycloakSession session, ComponentModel model) {
    log.debug("create()...");
    return new SierraUserStorageProvider(session, model);
  }

  @Override
  public String getId() {
    log.debug("SIERRA User Provider::getId");
    return PROVIDER_ID;
  }

  @Override
  public String getHelpText() {
    return "SIERRA User Provider";
  }

  @Override
  public List<ProviderConfigProperty> getConfigProperties() {
    return ProviderConfigurationBuilder.create()
      .property(SierraProviderConstants.BASE_URL, "Base URL", "OKAPI Base URL", ProviderConfigProperty.STRING_TYPE, "", null)
      .property(SierraProviderConstants.CLIENT_KEY, "Client Key", "Sierra API Client Key", ProviderConfigProperty.STRING_TYPE, "", null)
      .property(SierraProviderConstants.SECRET, "Secret", "Sierra API Secret", ProviderConfigProperty.STRING_TYPE, "", null)
      .build();
  }

  @Override
  public void validateConfiguration(KeycloakSession session, RealmModel realm, ComponentModel config) throws ComponentValidationException {
    if ( StringUtil.isBlank(config.get(SierraProviderConstants.BASE_URL))
      || StringUtil.isBlank(config.get(SierraProviderConstants.CLIENT_KEY))
      || StringUtil.isBlank(config.get(SierraProviderConstants.SECRET))) {
      throw new ComponentValidationException("Configuration not properly set, please verify.");
    }
  }

    @Override
    public void init(Config.Scope scope) {
        log.debugf("init");
    }

    @Override
    public void postInit(KeycloakSessionFactory keycloakSessionFactory) {
        log.debugf("postInit");
    }
}
