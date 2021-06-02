#!/usr/bin/env bash

REALM_NAME=example1

echo ""
echo "================================="
echo "setting up realm ${REALM_NAME}..."
echo "================================="
echo ""

# create realm
REALM_ID=$(createRealm $REALM_NAME)

# --- Realm Settings ---------------------------------------------------------------------------------------------

# general, login, theme, email, tokens
$KCADM update realms/${REALM_NAME} -s displayName="${REALM_NAME}" -s enabled=true \
  -s registrationAllowed=true -s registrationEmailAsUsername=true -s loginWithEmailAllowed=true \
  -s accountTheme=keycloak -s internationalizationEnabled=true -s 'supportedLocales=["de", "en", "it", "fr"]' -s 'defaultLocale="de"'


# --- Events ------------------------------------------------------------------------------------------------------

# store events (login & admin)
$KCADM update events/config -r ${REALM_NAME} -s 'eventsListeners=["jboss-logging"]' \
  -s adminEventsEnabled=true -s adminEventsDetailsEnabled=true \
  -s eventsEnabled=true -s eventsExpiration=172800 -s 'enabledEventTypes=["LOGIN", "LOGOUT", "CODE_TO_TOKEN", "REFRESH_TOKEN", "REFRESH_TOKEN", "LOGIN_ERROR","REGISTER_ERROR","LOGOUT_ERROR","CODE_TO_TOKEN_ERROR","CLIENT_LOGIN_ERROR","FEDERATED_IDENTITY_LINK_ERROR","REMOVE_FEDERATED_IDENTITY_ERROR","UPDATE_EMAIL_ERROR","UPDATE_PROFILE_ERROR","UPDATE_PASSWORD_ERROR","UPDATE_TOTP_ERROR","VERIFY_EMAIL_ERROR","REMOVE_TOTP_ERROR","SEND_VERIFY_EMAIL_ERROR","SEND_RESET_PASSWORD_ERROR","SEND_IDENTITY_PROVIDER_LINK_ERROR","RESET_PASSWORD_ERROR","IDENTITY_PROVIDER_FIRST_LOGIN_ERROR","IDENTITY_PROVIDER_POST_LOGIN_ERROR","CUSTOM_REQUIRED_ACTION_ERROR","EXECUTE_ACTIONS_ERROR","CLIENT_REGISTER_ERROR","CLIENT_UPDATE_ERROR","CLIENT_DELETE_ERROR"]'


# --- Authentication ---------------------------------------------------------------------------------------------------

## authentication

# set default browser flow
$KCADM update realms/${REALM_NAME} -s browserFlow=browser
# create IPS browser flow
TOP_LEVEL_FLOW_NAME=browser2
deleteTopLevelFlow $REALM_NAME $TOP_LEVEL_FLOW_NAME
createTopLevelFlow $REALM_NAME $TOP_LEVEL_FLOW_NAME
# SSO cookie support
createExecution $REALM_NAME $TOP_LEVEL_FLOW_NAME auth-cookie ALTERNATIVE
# external identity provider support
createExecution $REALM_NAME $TOP_LEVEL_FLOW_NAME identity-provider-redirector ALTERNATIVE
# form for user interaction
FORMS_SUBFLOW_NAME="Forms"
createSubflow $REALM_NAME ${TOP_LEVEL_FLOW_NAME} $TOP_LEVEL_FLOW_NAME $FORMS_SUBFLOW_NAME ALTERNATIVE
# no-operation-authenticator from extensions
createExecution ${REALM_NAME} ${FORMS_SUBFLOW_NAME} no-operation-authenticator REQUIRED
# email/password form
createExecution ${REALM_NAME} ${FORMS_SUBFLOW_NAME} auth-username-password-form REQUIRED

# the new flow should be used for browser based authentications
$KCADM update realms/$REALM_NAME -s browserFlow=${TOP_LEVEL_FLOW_NAME}
