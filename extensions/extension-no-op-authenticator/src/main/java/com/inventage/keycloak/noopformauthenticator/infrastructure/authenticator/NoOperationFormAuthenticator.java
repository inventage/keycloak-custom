package com.inventage.keycloak.noopformauthenticator.infrastructure.authenticator;

import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import javax.ws.rs.core.MultivaluedMap;


/**
 *
 * see https://freemarker.apache.org/docs/index.html
 */
public class NoOperationFormAuthenticator implements Authenticator {

    public static final String LIVING_PLACE_PARAMETER = "livingPlace"; // value must be the same as of input.name in living-place.ftl

    private static final Logger LOG = Logger.getLogger(NoOperationFormAuthenticator.class);

    @Override
    public void authenticate(AuthenticationFlowContext authenticationFlowContext) {
        LOG.debugf("authenticate");
        final LoginFormsProvider formsProvider = authenticationFlowContext.form();
        formsProvider.setAttribute("formModel", new NoOperationFormModel());
        authenticationFlowContext.challenge(formsProvider.createForm("living-place.ftl"));
    }

    @Override
    public void action(AuthenticationFlowContext authenticationFlowContext) {
        LOG.debugf("action");
        final MultivaluedMap<String, String> decodedFormParameters = authenticationFlowContext.getHttpRequest().getDecodedFormParameters();
        if (decodedFormParameters.containsKey(LIVING_PLACE_PARAMETER)) {
            final String livingPlace = decodedFormParameters.getFirst(LIVING_PLACE_PARAMETER);
            LOG.debugf("action: received value of parameter '%s': '%s'", LIVING_PLACE_PARAMETER, livingPlace);
        }
        authenticationFlowContext.success();
    }

    @Override
    public boolean requiresUser() {
        LOG.debugf("requiresUser");
        return false;
    }

    @Override
    public boolean configuredFor(KeycloakSession keycloakSession, RealmModel realmModel, UserModel userModel) {
        LOG.debugf("configuredFor");
        return false;
    }

    @Override
    public void setRequiredActions(KeycloakSession keycloakSession, RealmModel realmModel, UserModel userModel) {
        LOG.debugf("setRequiredActions");
        //NOP
    }

    @Override
    public void close() {
        LOG.debugf("close");
        //NOP
    }
}
