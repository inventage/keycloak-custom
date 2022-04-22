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

mergeJsons() {
    echo "--- Merging import files"
    echo ""

    BASEDIR=$(dirname "$0")

    # run folder merge
    PATH_TO_CONFIG_JSON=$BASEDIR/../setup
    java -jar $BASEDIR/client/filemerge-${jsondeepmerge.version}-runner.jar $PATH_TO_CONFIG_JSON/default $PATH_TO_CONFIG_JSON/override $PATH_TO_CONFIG_JSON
}

runKeycloakConfigCli() {
  echo ""
  echo "--- Running Keycloak Config CLI"
  echo ""

  BASEDIR=$(dirname "$0")

  # run keycloak-config-cli
  java -jar $BASEDIR/client/keycloak-config-cli-${keycloak-config-cli.version}.jar \
      --keycloak.url=http://localhost:8080/ \
      --keycloak.ssl-verify=true \
      --keycloak.user=${KEYCLOAK_ADMIN} \
      --keycloak.password=${KEYCLOAK_ADMIN_PASSWORD} \
      --keycloak.availability-check.enabled=true \
      --keycloak.availability-check.timeout=300s \
      --import.var-substitution.enabled=true \
      --import.managed.client=no-delete \
      --import.managed.client-scope=no-delete \
      --import.managed.client-scope-mapping=no-delete \
      --import.files.locations=$PATH_TO_CONFIG_JSON/*.json
}

runKeycloakCli() {
  BASEDIR=$(dirname "$0")
  source ${BASEDIR}/keycloak-cli-helpers.sh

  if [ "$KCADM" == "" ]; then
      KCADM=${BASEDIR}/kcadm.sh
      echo "Using $KCADM as the admin CLI."
  fi

  ${KCADM} config credentials --server http://localhost:8080 --user ${KEYCLOAK_ADMIN} --password ${KEYCLOAK_ADMIN_PASSWORD} --realm master

  source ${BASEDIR}/realms.sh
}

echo " "
echo "----------------- KEYCLOAK CONFIG CLI ------------------"
echo " "
mergeJsons
runKeycloakConfigCli

echo " "
echo "----------------- KEYCLOAK CLI ------------------"
echo " "
runKeycloakCli


echo " "
echo "--------------- KEYCLOAK SETUP FINISHED ----------------"
echo " "
