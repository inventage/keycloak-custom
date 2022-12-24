package org.olf.folio.adaptor;

import static org.keycloak.provider.ProviderConfigProperty.STRING_TYPE;
import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.authenticators.conditional.ConditionalAuthenticator;
import org.keycloak.authentication.authenticators.conditional.ConditionalAuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

import java.util.Collections;
import java.util.List;
import org.jboss.logging.Logger;

public class FolioAuthenticatorFactory implements ConditionalAuthenticatorFactory {

    public static final String PROVIDER_ID = "folio_login";
    static final String PREFIX = "folio_login";
    private static final Logger LOG = Logger.getLogger(FolioAuthenticatorFactory.class);

    private static final FolioAuthenticator SINGLETON = new FolioAuthenticator();
    private static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED,
            AuthenticationExecutionModel.Requirement.ALTERNATIVE,
            AuthenticationExecutionModel.Requirement.CONDITIONAL,
            AuthenticationExecutionModel.Requirement.DISABLED
    };


    private static final List<ProviderConfigProperty> PROVIDER_CONFIG_PROPS = Collections.unmodifiableList(ProviderConfigurationBuilder.create()
        .property().name("FOLIO base url")
        .label("FOLIO OKAPI URL")
        .helpText("The base URL of the FOLIO API")
        .type(ProviderConfigProperty.STRING_TYPE).add()
        .property().name("FOLIO tenant")
        .label("FOLIO TENANT")
        .helpText("The tenant ID")
        .type(ProviderConfigProperty.STRING_TYPE).add()
        .build()
      );

    @Override
    public String getDisplayType() {
        LOG.debugf("getDisplayType");
        return "FOLIO Authenticator";
    }

    @Override
    public String getReferenceCategory() {
        LOG.debugf("getReferenceCategory");
        return "condition";
    }

    @Override
    public boolean isConfigurable() {
        LOG.debugf("isConfigurable");
        // FOR NOW
        return false;
    }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        LOG.debugf("getRequirementChoices");
        return REQUIREMENT_CHOICES;
    }

    @Override
    public boolean isUserSetupAllowed() {
        LOG.debugf("isUserSetupAllowed");
        return true;
    }

    @Override
    public String getHelpText() {
        return "Allow login with only folio authentication";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
      return PROVIDER_CONFIG_PROPS;
    }

    /*
    @Override
    public Authenticator create(KeycloakSession session) {
        LOG.debugf("create");
        return SINGLETON;
    }
    */

    @Override
    public ConditionalAuthenticator getSingleton() {
      return SINGLETON;
    }

    @Override
    public void init(Config.Scope scope) {
        LOG.debugf("init");
    }

    @Override
    public void postInit(KeycloakSessionFactory keycloakSessionFactory) {
        LOG.debugf("postInit");
    }

    @Override
    public void close() {
        LOG.debugf("close");
    }

    @Override
    public String getId() {
        LOG.debugf("getId");
        return PROVIDER_ID;
    }

}
