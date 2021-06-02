#!/usr/bin/env bash

##########
#
# This script does the configuration of keycloak, so we don't have to do it in the UI.
#
#########

trap 'exit' ERR

echo " "
echo "========================================================"
echo "==         STARTING KEYCLOAK CONFIGURATION            =="
echo "========================================================"
echo " "

BASEDIR=$(dirname "$0")
source ${BASEDIR}/keycloak-configuration-helpers.sh

if [ "$KCADM" == "" ]; then
    KCADM=$KEYCLOAK_HOME/bin/kcadm.sh
    echo "Using $KCADM as the admin CLI."
fi

${KCADM} config credentials --server http://localhost:8080/auth --user ${KEYCLOAK_USER} --password ${KEYCLOAK_PASSWORD} --realm master

source ${BASEDIR}/realms.sh


echo " "
echo "========================================================"
echo "==            KEYCLOAK CONFIGURATION DONE             =="
echo "========================================================"
echo " "
