#!/usr/bin/env bash

REALM_NAME='master'

echo ""
echo "================================="
echo "setting up realm $REALM_NAME..."
echo "================================="
echo ""

# --- Events ------------------------------------------------------------------------------------------------------

# store events (login & admin)
$KCADM update events/config -r $REALM_NAME -s 'eventsListeners=["jboss-logging"]' \
  -s adminEventsEnabled=true -s adminEventsDetailsEnabled=true \
  -s eventsEnabled=true -s eventsExpiration=172800 -s 'enabledEventTypes=["LOGIN", "LOGOUT", "CODE_TO_TOKEN", "REFRESH_TOKEN", "REFRESH_TOKEN", "LOGIN_ERROR","REGISTER_ERROR","LOGOUT_ERROR","CODE_TO_TOKEN_ERROR","CLIENT_LOGIN_ERROR","FEDERATED_IDENTITY_LINK_ERROR","REMOVE_FEDERATED_IDENTITY_ERROR","UPDATE_EMAIL_ERROR","UPDATE_PROFILE_ERROR","UPDATE_PASSWORD_ERROR","UPDATE_TOTP_ERROR","VERIFY_EMAIL_ERROR","REMOVE_TOTP_ERROR","SEND_VERIFY_EMAIL_ERROR","SEND_RESET_PASSWORD_ERROR","SEND_IDENTITY_PROVIDER_LINK_ERROR","RESET_PASSWORD_ERROR","IDENTITY_PROVIDER_FIRST_LOGIN_ERROR","IDENTITY_PROVIDER_POST_LOGIN_ERROR","CUSTOM_REQUIRED_ACTION_ERROR","EXECUTE_ACTIONS_ERROR","CLIENT_REGISTER_ERROR","CLIENT_UPDATE_ERROR","CLIENT_DELETE_ERROR"]'
