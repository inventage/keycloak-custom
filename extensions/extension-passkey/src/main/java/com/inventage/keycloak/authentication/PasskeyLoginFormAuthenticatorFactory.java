package com.inventage.keycloak.authentication;

import com.google.auto.service.AutoService;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.authenticators.browser.WebAuthnPasswordlessAuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;


@AutoService(org.keycloak.authentication.AuthenticatorFactory.class)
public class PasskeyLoginFormAuthenticatorFactory extends WebAuthnPasswordlessAuthenticatorFactory {

    public static final String PROVIDER_ID = "passkey-login-form";
    private static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED,
            AuthenticationExecutionModel.Requirement.ALTERNATIVE
    };
    @Override
    public Authenticator create(KeycloakSession session) {
        return new PasskeyLoginFormAuthenticator(session);
    }
    @Override
    public String getId() {
        return PROVIDER_ID;
    }
    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }
    @Override
    public String getDisplayType() {
        return "Passkey Login Form";
    }

    @Override
    public String getHelpText() {
        return "Passkey Tutorial: Should not be used in combination with built-in Authenticators/Forms. Initial login page with the possibility to sign in with passkey or password";
    }

}