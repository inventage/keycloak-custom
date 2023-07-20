package com.inventage.keycloak.authentication;

import com.webauthn4j.data.AuthenticationRequest;
import com.webauthn4j.data.client.Origin;
import com.webauthn4j.data.client.challenge.Challenge;
import com.webauthn4j.data.client.challenge.DefaultChallenge;
import com.webauthn4j.server.ServerProperty;
import com.webauthn4j.util.exception.WebAuthnException;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.keycloak.WebAuthnConstants;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.authenticators.browser.AbstractUsernameFormAuthenticator;
import org.keycloak.authentication.authenticators.browser.WebAuthnPasswordlessAuthenticator;
import org.keycloak.authentication.forms.RegistrationPage;
import org.keycloak.common.util.Base64Url;
import org.keycloak.common.util.UriUtils;
import org.keycloak.credential.CredentialModel;
import org.keycloak.credential.WebAuthnCredentialModelInput;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.forms.login.freemarker.model.WebAuthnAuthenticatorsBean;
import org.keycloak.models.*;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.services.messages.Messages;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import static org.keycloak.WebAuthnConstants.AUTH_ERR_DETAIL_LABEL;
import static org.keycloak.WebAuthnConstants.AUTH_ERR_LABEL;
import static org.keycloak.services.messages.Messages.*;
import static org.keycloak.services.messages.Messages.WEBAUTHN_ERROR_USER_NOT_FOUND;

/**
 * This class extends the built-in WebAuthnPasswordlessAuthenticator class. We modified the following functionalities:
 * (1): Instead of rendering the built-in webauthn form, we render our customized ftl called username-with-challenge.
 * (2): Our webauthn challenge does not presume a specific user.
 * <p>
 * This class contains all the logic when the passkey-login-form.ftl file is rendered or interacted with.
 * passkey-login-form.ftl displays a page with two options for signing in (with passkey or otherwise).
 * <p>
 * This authenticator is supposed to be used in the browser-flow.
 * Purpose: We enable two options for the user to sign in. Either with passkey or password.
 * The rendered custom form contains the webauthn challenge for the case that you want to sign in with passkey.
 * If chosen otherwise we continue to the next authenticator configured in the browser-flow.
 */
public class PasskeyLoginFormAuthenticator extends WebAuthnPasswordlessAuthenticator {

    private static final String TPL_CODE = "passkey-login-form.ftl";
    private static final String USERNAME_KEY = "username";
    private KeycloakSession session;

    public PasskeyLoginFormAuthenticator(KeycloakSession session) {
        super(session);
        this.session = session;
    }

    // renders initial login page.
    @Override
    public void authenticate(AuthenticationFlowContext context) {
        // The authenticate method is identical with the built-in method of org.keycloak.authentication.authenticators.browser.WebAuthnAuthenticator
        // except that we use our own ftl-template "passkey-login-form.ftl", that renders our custom login page.
        LoginFormsProvider form = createLoginFormWithWebauthnChallenge(context);
        if (form != null) {
            context.challenge(form.createForm(TPL_CODE));
        }
    }

    /**
     * Copied from org.keycloak.authentication.authenticators.browser.WebAuthnAuthenticator#authenticate
     * Encodes the Webauthn challenge in the ftl template, that will be displayed to the user.
     **/
    private LoginFormsProvider createLoginFormWithWebauthnChallenge(AuthenticationFlowContext context) {
        LoginFormsProvider form = context.form();

        Challenge challenge = new DefaultChallenge();
        String challengeValue = Base64Url.encode(challenge.getValue());
        context.getAuthenticationSession().setAuthNote(WebAuthnConstants.AUTH_CHALLENGE_NOTE, challengeValue);
        form.setAttribute(WebAuthnConstants.CHALLENGE, challengeValue);

        WebAuthnPolicy policy = getWebAuthnPolicy(context);
        String rpId = getRpID(context);
        form.setAttribute(WebAuthnConstants.RP_ID, rpId);
        form.setAttribute(WebAuthnConstants.CREATE_TIMEOUT, policy.getCreateTimeout());

        UserModel user = context.getUser();
        boolean isUserIdentified = false;
        if (user != null) {
            // in 2 Factor Scenario where the user has already been identified
            WebAuthnAuthenticatorsBean authenticators = new WebAuthnAuthenticatorsBean(context.getSession(), context.getRealm(), user, getCredentialType());
            if (authenticators.getAuthenticators().isEmpty()) {
                // require the user to register webauthn authenticator
                return null;
            }
            isUserIdentified = true;
            form.setAttribute(WebAuthnConstants.ALLOWED_AUTHENTICATORS, authenticators);
        } else {
            // in ID-less & Password-less Scenario
            // NOP
        }
        form.setAttribute(WebAuthnConstants.IS_USER_IDENTIFIED, Boolean.toString(isUserIdentified));

        // read options from policy
        String userVerificationRequirement = policy.getUserVerificationRequirement();
        form.setAttribute(WebAuthnConstants.USER_VERIFICATION, userVerificationRequirement);
        form.setAttribute(WebAuthnConstants.SHOULD_DISPLAY_AUTHENTICATORS, shouldDisplayAuthenticators(context));
        return form;
    }

    // validates user data transmitted as form parameters
    @Override
    public void action(AuthenticationFlowContext context) {
        // Retrieve parameters from incoming http request.
        MultivaluedMap<String, String> params = context.getHttpRequest().getDecodedFormParameters();

        // check if user tries to sign in with webauthn(passkey)
        boolean isPasskeyLogin = isPasskeyLogin(params);
        if (isPasskeyLogin) {
            // Check if passkey login failed
            boolean webAuthnFailed = hasWebAuthnFailed(params);
            if (webAuthnFailed) {
                // Webauthn inserts the error in the http request.
                String error = params.getFirst(WebAuthnConstants.ERROR);
                handleWebauthnError(context, error);
                return;
            }
            // verify webauthn challenge response. If successful, the user has successfully signed in to his user account
            verifyWebauthnChallengeResponse(context, params);
            return;
        }

        // user wants to sign in with username and password
        String username = params.getFirst(USERNAME_KEY).trim();
        UserModel user = retrieveUser(context, username);

        // no user with this username has been found
        if (user == null) {
            handleInvalidUsernameError(context);
        } else {
            boolean noPassword = hasNoPasswordSetup(user);
            if (noPassword) {
                handleNoPasswordError(context);
                return;
            }

            //User is required for the built-in password form authenticator to work: org.keycloak.authentication.authenticators.browser.PasswordForm
            context.setUser(user);
            //Call attempted to signal keycloak to call the next "Alternative" in the authentication flow.
            context.attempted();
        }
    }

    /**
     * @param params
     * @return true, if the user attempted to sign in with passkey. This can be determined by checking the submitted form parameters.
     */
    private boolean isPasskeyLogin(MultivaluedMap<String, String> params) {
        return (params.getFirst(WebAuthnConstants.CREDENTIAL_ID) != null && params.getFirst(WebAuthnConstants.AUTHENTICATOR_DATA) != null && params.getFirst(WebAuthnConstants.CLIENT_DATA_JSON) != null
                && params.getFirst(WebAuthnConstants.SIGNATURE) != null && params.getFirst(WebAuthnConstants.USER_HANDLE) != null) || hasWebAuthnFailed(params);
    }

    /**
     * @param params
     * @return true, if WebAuthn submits an error to Keycloak.
     */
    private boolean hasWebAuthnFailed(MultivaluedMap<String, String> params) {
        String error = params.getFirst(WebAuthnConstants.ERROR);
        if (error != null) {
            return !error.isEmpty();
        }
        return false;
    }

    private void handleWebauthnError(AuthenticationFlowContext context, String error) {
        LoginFormsProvider form = createLoginFormWithWebauthnChallenge(context);
        if (form != null) {
            context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS,
                    form.setError(error).createForm(TPL_CODE));
        }
    }

    // verify webauthn challenge response. If successful, the user has successfully signed in to his user account.
    // Copied from org.keycloak.authentication.authenticators.browser.WebAuthnAuthenticator#action. Difference: Error message can be displayed on our custom "ftl"-file
    private void verifyWebauthnChallengeResponse(AuthenticationFlowContext context, MultivaluedMap<String, String> params) {
        context.getEvent().detail(Details.CREDENTIAL_TYPE, getCredentialType());

        String baseUrl = UriUtils.getOrigin(context.getUriInfo().getBaseUri());
        String rpId = getRpID(context);

        Origin origin = new Origin(baseUrl);
        Challenge challenge = new DefaultChallenge(context.getAuthenticationSession().getAuthNote(WebAuthnConstants.AUTH_CHALLENGE_NOTE));
        ServerProperty server = new ServerProperty(origin, rpId, challenge, null);

        byte[] credentialId = Base64Url.decode(params.getFirst(WebAuthnConstants.CREDENTIAL_ID));
        byte[] clientDataJSON = Base64Url.decode(params.getFirst(WebAuthnConstants.CLIENT_DATA_JSON));
        byte[] authenticatorData = Base64Url.decode(params.getFirst(WebAuthnConstants.AUTHENTICATOR_DATA));
        byte[] signature = Base64Url.decode(params.getFirst(WebAuthnConstants.SIGNATURE));

        final String userHandle = params.getFirst(WebAuthnConstants.USER_HANDLE);
        final String userId;
        // existing User Handle means that the authenticator used Resident Key supported public key credential
        if (userHandle == null || userHandle.isEmpty()) {
            // Resident Key not supported public key credential was used
            // so rely on the user set in a previous step (if available)
            if (context.getUser() != null) {
                userId = context.getUser().getId();
            } else {
                setErrorResponse(context, WEBAUTHN_ERROR_USER_NOT_FOUND,
                        "Webauthn credential provided doesn't include user id and user id wasn't provided in a previous step");
                return;
            }
        } else {
            // decode using the same charset as it has been encoded (see: WebAuthnRegister.java)
            userId = new String(Base64Url.decode(userHandle), StandardCharsets.UTF_8);
            if (context.getUser() != null) {
                // Resident Key supported public key credential was used,
                // so need to confirm whether the already authenticated user is equals to one authenticated by the webauthn authenticator
                String firstAuthenticatedUserId = context.getUser().getId();
                if (firstAuthenticatedUserId != null && !firstAuthenticatedUserId.equals(userId)) {
                    context.getEvent()
                            .detail(WebAuthnConstants.FIRST_AUTHENTICATED_USER_ID, firstAuthenticatedUserId)
                            .detail(WebAuthnConstants.AUTHENTICATED_USER_ID, userId);
                    setErrorResponse(context, WEBAUTHN_ERROR_DIFFERENT_USER, null);
                    return;
                }
            } else {
                // Resident Key supported public key credential was used,
                // and the user has not yet been identified
                // so rely on the user authenticated by the webauthn authenticator
                // NOP
            }
        }

        boolean isUVFlagChecked = false;
        String userVerificationRequirement = getWebAuthnPolicy(context).getUserVerificationRequirement();
        if (WebAuthnConstants.OPTION_REQUIRED.equals(userVerificationRequirement)) isUVFlagChecked = true;

        UserModel user = session.users().getUserById(context.getRealm(), userId);

        AuthenticationRequest authenticationRequest = new AuthenticationRequest(
                credentialId,
                authenticatorData,
                clientDataJSON,
                signature
        );

        WebAuthnCredentialModelInput.KeycloakWebAuthnAuthenticationParameters authenticationParameters = new WebAuthnCredentialModelInput.KeycloakWebAuthnAuthenticationParameters(
                server,
                isUVFlagChecked
        );

        WebAuthnCredentialModelInput cred = new WebAuthnCredentialModelInput(getCredentialType());

        cred.setAuthenticationRequest(authenticationRequest);
        cred.setAuthenticationParameters(authenticationParameters);
        String encodedCredentialID = Base64Url.encode(credentialId);

        if (user == null) {
            context.getEvent()
                    .detail(WebAuthnConstants.AUTHENTICATED_USER_ID, userId)
                    .detail(WebAuthnConstants.PUBKEY_CRED_ID_ATTR, encodedCredentialID);
            setErrorResponse(context, WEBAUTHN_ERROR_USER_NOT_FOUND, null);
            return;
        }

        boolean result = false;
        try {
            result = user.credentialManager().isValid(cred);
        } catch (WebAuthnException wae) {
            setErrorResponse(context, WEBAUTHN_ERROR_AUTH_VERIFICATION, wae.getMessage());
            return;
        }

        if (result) {
            String isUVChecked = Boolean.toString(isUVFlagChecked);
            context.setUser(user);
            context.getEvent()
                    .detail(WebAuthnConstants.USER_VERIFICATION_CHECKED, isUVChecked)
                    .detail(WebAuthnConstants.PUBKEY_CRED_ID_ATTR, encodedCredentialID);
            context.success();
        } else {
            context.getEvent()
                    .detail(WebAuthnConstants.AUTHENTICATED_USER_ID, userId)
                    .detail(WebAuthnConstants.PUBKEY_CRED_ID_ATTR, encodedCredentialID);
            setErrorResponse(context, WEBAUTHN_ERROR_USER_NOT_FOUND, null);
        }
    }

    private UserModel retrieveUser(AuthenticationFlowContext context, String username) {
        context.getEvent().detail(Details.USERNAME, username);
        context.getAuthenticationSession().setAuthNote(AbstractUsernameFormAuthenticator.ATTEMPTED_USERNAME, username);
        UserModel user;
        try {
            user = KeycloakModelUtils.findUserByNameOrEmail(context.getSession(), context.getRealm(), username);
        } catch (ModelDuplicateException mde) {
            throw new RuntimeException(mde);
        }
        return user;
    }

    private void handleInvalidUsernameError(AuthenticationFlowContext context) {
        LoginFormsProvider form = createLoginFormWithWebauthnChallenge(context);
        if (form != null) {
            context.failureChallenge(AuthenticationFlowError.INVALID_CLIENT_CREDENTIALS,
                    form.setErrors(List.of(new FormMessage(RegistrationPage.FIELD_USERNAME, Messages.INVALID_USERNAME))).createForm(TPL_CODE));
        }
    }

    private boolean hasNoPasswordSetup(UserModel user) {
        List<CredentialModel> credentialModels = user.credentialManager().getStoredCredentialsStream().collect(Collectors.toList());
        return !credentialModels.stream().anyMatch((cred -> cred.getType().equals(PasswordCredentialModel.TYPE)));
    }
    private void handleNoPasswordError(AuthenticationFlowContext context) {
        LoginFormsProvider form = createLoginFormWithWebauthnChallenge(context);
        if (form != null) {
            context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS,
                    form.setError("This user has not setup a password").createForm(TPL_CODE));
        }
    }

    private void setErrorResponse(AuthenticationFlowContext context, final String errorCase, final String errorMessage) {
        Response errorResponse;
        switch (errorCase) {
            case WEBAUTHN_ERROR_REGISTRATION:
                context.getEvent()
                        .detail(AUTH_ERR_LABEL, errorCase)
                        .error(Errors.INVALID_USER_CREDENTIALS);
                errorResponse = createErrorResponse(context, errorCase);
                context.failure(AuthenticationFlowError.INVALID_CREDENTIALS, errorResponse);
                break;
            case WEBAUTHN_ERROR_API_GET:
                context.getEvent()
                        .detail(AUTH_ERR_LABEL, errorCase)
                        .detail(AUTH_ERR_DETAIL_LABEL, errorMessage)
                        .error(Errors.NOT_ALLOWED);
                errorResponse = createErrorResponse(context, errorCase);
                context.failure(AuthenticationFlowError.INVALID_USER, errorResponse);
                break;
            case WEBAUTHN_ERROR_DIFFERENT_USER:
                context.getEvent()
                        .detail(AUTH_ERR_LABEL, errorCase)
                        .error(Errors.DIFFERENT_USER_AUTHENTICATED);
                errorResponse = createErrorResponse(context, errorCase);
                context.failure(AuthenticationFlowError.USER_CONFLICT, errorResponse);
                break;
            case WEBAUTHN_ERROR_AUTH_VERIFICATION:
                context.getEvent()
                        .detail(AUTH_ERR_LABEL, errorCase)
                        .detail(AUTH_ERR_DETAIL_LABEL, errorMessage)
                        .error(Errors.INVALID_USER_CREDENTIALS);
                errorResponse = createErrorResponse(context, errorCase);
                context.failure(AuthenticationFlowError.INVALID_USER, errorResponse);
                break;
            case WEBAUTHN_ERROR_USER_NOT_FOUND:
                context.getEvent()
                        .detail(AUTH_ERR_LABEL, errorCase)
                        .error(Errors.USER_NOT_FOUND);
                errorResponse = createErrorResponse(context, errorCase);
                context.failure(AuthenticationFlowError.UNKNOWN_USER, errorResponse);
                break;
            default:
                // NOP
        }
    }

    private Response createErrorResponse(AuthenticationFlowContext context, final String errorCase) {
        Response errorResponse = createLoginFormWithWebauthnChallenge(context).setError(errorCase).createForm(TPL_CODE);
        return errorResponse;
    }


}
