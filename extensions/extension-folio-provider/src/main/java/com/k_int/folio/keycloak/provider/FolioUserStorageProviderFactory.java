package com.k_int.folio.keycloak.provider;

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
import static java.util.Arrays.asList;

public class FolioUserStorageProviderFactory implements UserStorageProviderFactory<FolioUserStorageProvider> {

  public static final String PROVIDER_ID = "folio-user";
  private static final Logger log = Logger.getLogger(FolioUserStorageProviderFactory.class);

  @Override
  public FolioUserStorageProvider create(KeycloakSession session, ComponentModel model) {
    log.debug("create()...");
    return new FolioUserStorageProvider(session, model);
  }

  @Override
  public String getId() {
    log.debug("FOLIO User Provider::getId");
    return PROVIDER_ID;
  }

  @Override
  public String getHelpText() {
    return "FOLIO User Provider";
  }

  @Override
  public List<ProviderConfigProperty> getConfigProperties() {
    return ProviderConfigurationBuilder.create()
      .property(FolioProviderConstants.BASE_URL, "Base URL", "OKAPI Base URL", ProviderConfigProperty.STRING_TYPE, "", null)
      .property(FolioProviderConstants.TENANT, "Tenant", "OKAPI Tenant", ProviderConfigProperty.STRING_TYPE, "", null)
      .property(FolioProviderConstants.AUTH_USERNAME, "Username", "Username for OKAPI at the API", ProviderConfigProperty.STRING_TYPE, "", null)
      .property(FolioProviderConstants.AUTH_PASSWORD, "Password", "Password for OKAPI at the API", ProviderConfigProperty.PASSWORD, "", null)
      .property(FolioProviderConstants.HOME_LIBRARY_METHOD, "Home Library Derivation", "How should this realm determin the home library of the patron", 
                ProviderConfigProperty.LIST_TYPE, "Static",
                asList("Static", "From Custom Property"))
      .property(FolioProviderConstants.LOCAL_SYSTEM_CODE, "Local System Code", "Local System Code", ProviderConfigProperty.STRING_TYPE, "", null)
      .property(FolioProviderConstants.DEFAULT_HOME_LIBRARY, "Default Home Library", "Default Home Library", ProviderConfigProperty.STRING_TYPE, "", null)
      .build();
  }

  @Override
  public void validateConfiguration(KeycloakSession session, RealmModel realm, ComponentModel config) throws ComponentValidationException {
    if ( StringUtil.isBlank(config.get(FolioProviderConstants.BASE_URL))
      || StringUtil.isBlank(config.get(FolioProviderConstants.TENANT))
      || StringUtil.isBlank(config.get(FolioProviderConstants.AUTH_USERNAME))
      || StringUtil.isBlank(config.get(FolioProviderConstants.HOME_LIBRARY_METHOD))
      || StringUtil.isBlank(config.get(FolioProviderConstants.LOCAL_SYSTEM_CODE))
      || StringUtil.isBlank(config.get(FolioProviderConstants.DEFAULT_HOME_LIBRARY))
      || StringUtil.isBlank(config.get(FolioProviderConstants.AUTH_PASSWORD))) {
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
