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

import java.util.Collections;
import java.util.List;
import org.jboss.logging.Logger;

public class ConditionalFolioFactory implements ConditionalAuthenticatorFactory {

    public static final String ID = "conditional_folio_login";
    static final String PREFIX = "folio_login";
    private static final Logger LOG = Logger.getLogger(ConditionalFolioFactory.class);

    private static final FolioAuthenticator SINGLETON = new FolioAuthenticator();
    private static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED,
            AuthenticationExecutionModel.Requirement.ALTERNATIVE,
            AuthenticationExecutionModel.Requirement.CONDITIONAL,
            AuthenticationExecutionModel.Requirement.DISABLED
    };

    @Override
    public Authenticator create(KeycloakSession session) {
        LOG.debugf("create");
        return SINGLETON;
    }

    @Override
    public ConditionalAuthenticator getSingleton() {
        LOG.debugf("getSingleton");
        return SINGLETON;
    }

    @Override
    public String getDisplayType() {
        LOG.debugf("getDisplayType");
        return "FOLIO Authenticator";
    }

    @Override
    public String getReferenceCategory() {
        LOG.debugf("getReferenceCategory");
        return null;
    }

    @Override
    public boolean isConfigurable() {
        LOG.debugf("isConfigurable");
        return true;
    }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        LOG.debugf("getRequirementChoices");
        return REQUIREMENT_CHOICES;
    }
    @Override
    public boolean isUserSetupAllowed() {
        LOG.debugf("isUserSetupAllowed");
        return false;
    }

    @Override
    public String getHelpText() {
        return "Allow login with only folio authentication";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        ProviderConfigProperty name = new ProviderConfigProperty();

        name.setType(STRING_TYPE);
        name.setName(PREFIX);
        name.setLabel("Folio");
        name.setHelpText("Folio");

        return Collections.singletonList(name);
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
        return ID;
    }

}
