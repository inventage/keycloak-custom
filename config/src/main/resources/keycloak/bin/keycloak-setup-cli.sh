#!/usr/bin/env bash

##########
#
# Setup Keycloak via CLI
#
#########

echo " "
echo "-------------------- keycloak-setup-cli.sh ---------------------"
echo " "

source ${BASEDIR}/keycloak-cli-helpers.sh

if [ "$KCADM" == "" ]; then
    KCADM=${BASEDIR}/kcadm.sh
    echo "Using $KCADM as the admin CLI."
fi

${KCADM} config credentials --server http://localhost:8080 --user ${KEYCLOAK_ADMIN} --password ${KEYCLOAK_ADMIN_PASSWORD} --realm master

