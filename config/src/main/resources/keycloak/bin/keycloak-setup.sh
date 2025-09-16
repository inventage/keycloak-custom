#!/usr/bin/env bash

##########
#
# This script does the setup of keycloak, so we don't have to do it in the UI.
#
#########

trap 'exit' ERR

echo " "
echo " "
echo "--------------- KEYCLOAK SETUP STARTING ----------------"
echo " "
echo " "

BASEDIR=$(dirname "$0")

runKeycloakConfigCli() {
  echo ""
  echo "--- Running Keycloak Config CLI"
  echo ""

  eval "KC_ADMIN_CLIENT_ID=${KEYCLOAK_CLIENT_ID_REF}"
  eval "KC_ADMIN_CLIENT_SECRET=${KEYCLOAK_CLIENT_SECRET_REF}"

  echo "Use keycloak-config-cli with client '${KC_ADMIN_CLIENT_ID}'"
  # run keycloak-config-cli
  java -jar "${BASEDIR}"/client/keycloak-config-cli-"${keycloak-config-cli.version}".jar \
      --keycloak.url=http://localhost:8080/ \
      --keycloak.ssl-verify=true \
      --keycloak.grant-type="${KEYCLOAK_GRANT_TYPE}" \
      --keycloak.client-id="${KC_ADMIN_CLIENT_ID}" \
      --keycloak.client-secret="${KC_ADMIN_CLIENT_SECRET}" \
      --keycloak.availability-check.enabled=true \
      --keycloak.availability-check.timeout=300s \
      --import.var-substitution.enabled=true \
      --import.managed.client=no-delete \
      --import.managed.client-scope=no-delete \
      --import.managed.client-scope-mapping=no-delete \
      --import.files.locations="${BASEDIR}"/../setup/*.json \
      --logging.level.root=info \
      --logging.level.keycloak-config-cli=info \
      --logging.level.realm-config=info
}

runKeycloakCli() {
  if [ "$KCADM" == "" ]; then
      KCADM="${BASEDIR}"/kcadm.sh
      KCADM_CONFIG="--config /tmp/.keycloak/kcadm.config"
      echo "Using $KCADM as the admin CLI."
  fi

  eval "KC_ADMIN_CLIENT_ID=${KEYCLOAK_CLIENT_ID_REF}"
  eval "KC_ADMIN_CLIENT_SECRET=${KEYCLOAK_CLIENT_SECRET_REF}"

  echo "Use kcadm with client '${KC_ADMIN_CLIENT_ID}'"
  ${KCADM} config credentials --server http://localhost:8080 --client ${KC_ADMIN_CLIENT_ID} --secret ${KC_ADMIN_CLIENT_SECRET} --realm master ${KCADM_CONFIG}

  # helper functions using kc admin cli
  source "${BASEDIR}"/keycloak-cli-helpers.sh

  # project specific configurations
  source "${BASEDIR}"/keycloak-cli-custom.sh
}

echo " "
echo "----------------- KEYCLOAK CONFIG CLI ------------------"
echo " "
runKeycloakConfigCli

echo " "
echo "----------------- KEYCLOAK CLI ------------------"
echo " "
runKeycloakCli

echo " "
echo "--------------- KEYCLOAK SETUP FINISHED ----------------"
echo " "
