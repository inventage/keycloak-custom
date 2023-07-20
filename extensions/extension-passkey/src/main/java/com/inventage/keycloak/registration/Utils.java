package com.inventage.keycloak.registration;

import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.FormContext;
import org.keycloak.events.Details;
import org.keycloak.events.EventType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.userprofile.UserProfile;
import org.keycloak.userprofile.UserProfileContext;
import org.keycloak.userprofile.UserProfileProvider;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

class Utils {

    private static final String KEYS_USERDATA = "keyUserdata";
    private static final String KEYS_USERDATA_SEPARATOR = ";";
    private static final List<String> DEFAULT_KEYS_USERDATA = List.of(UserModel.FIRST_NAME, UserModel.LAST_NAME, UserModel.EMAIL, UserModel.USERNAME);


    private Utils(){}

    /**
     * We store the user data entered in the registration form in the session notes.
     * This information will later be retrieved to create a user account.
     */
    static void storeUserDataInAuthSessionNotes(FormContext context){
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        AuthenticationSessionModel sessionModel = context.getAuthenticationSession();

        //We store each key
        String keys = Utils.serializeUserdataKeys(formData.keySet());
        sessionModel.setAuthNote(Utils.KEYS_USERDATA, keys);

        formData.forEach((key, value) -> {
            sessionModel.setAuthNote(key, formData.getFirst(key));
        });
    }

    /**
     * We retrieve the user data stored in the session notes and create a new user in this realm.
     */
    static void createUserFromAuthSessionNotes(AuthenticationFlowContext context) {
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        MultivaluedMap<String, String> userAttributes = new MultivaluedHashMap<>();

        AuthenticationSessionModel authenticationSession = context.getAuthenticationSession();
        List<String> keysUserdata = Utils.deserializeUserdataKeys(authenticationSession.getAuthNote(Utils.KEYS_USERDATA));

        //keys userdata is transmitted from the UserCreationPasskeyAction class.
        if (keysUserdata != null) {
            for (String key : keysUserdata) {
                String value = authenticationSession.getAuthNote(key);
                if (value != null) {
                    userAttributes.add(key, value);
                }
            }
        } // In case that another custom FormAction than UserCreationPasskey is used.
        else {
            for (String key : DEFAULT_KEYS_USERDATA) {
                String value = authenticationSession.getAuthNote(key);
                if (value != null) {
                    userAttributes.add(key, value);
                }
            }
        }

        String email = formData.getFirst(UserModel.EMAIL);
        String username = formData.getFirst(UserModel.USERNAME);

        if (context.getRealm().isRegistrationEmailAsUsername()) {
            username = email;
        }

        context.getEvent().detail(Details.USERNAME, username)
                .detail(Details.REGISTER_METHOD, "form")
                .detail(Details.EMAIL, email);

        context.getEvent().detail(Details.USERNAME, username)
                .detail(Details.REGISTER_METHOD, "form")
                .detail(Details.EMAIL, email);

        KeycloakSession session = context.getSession();

        UserProfileProvider profileProvider = session.getProvider(UserProfileProvider.class);
        UserProfile profile = profileProvider.create(UserProfileContext.REGISTRATION_USER_CREATION, userAttributes);
        UserModel user = profile.create();

        user.setEnabled(true);

        context.setUser(user);

        context.getAuthenticationSession().setClientNote(OIDCLoginProtocol.LOGIN_HINT_PARAM, username);

        context.getEvent().user(user);
        context.getEvent().success();
        context.newEvent().event(EventType.LOGIN);
        context.getEvent().client(context.getAuthenticationSession().getClient().getClientId())
                .detail(Details.REDIRECT_URI, context.getAuthenticationSession().getRedirectUri())
                .detail(Details.AUTH_METHOD, context.getAuthenticationSession().getProtocol());
        String authType = context.getAuthenticationSession().getAuthNote(Details.AUTH_TYPE);
        if (authType != null) {
            context.getEvent().detail(Details.AUTH_TYPE, authType);
        }
    }

    private static String serializeUserdataKeys(Collection<String> keys, String separator) {
        final StringBuilder key = new StringBuilder();
        keys.forEach((s -> key.append(s + separator)));
        return key.toString();
    }

    private static String serializeUserdataKeys(Collection<String> keys) {
        return serializeUserdataKeys(keys, KEYS_USERDATA_SEPARATOR);
    }

    private static List<String> deserializeUserdataKeys(String key, String separator) {
        if (key == null) {
            return Collections.emptyList();
        }
        return List.of(key.split(separator));
    }

    private static List<String> deserializeUserdataKeys(String key) {
        return deserializeUserdataKeys(key, KEYS_USERDATA_SEPARATOR);
    }
}
