#!/usr/bin/env bash

##########
#
# This script contains function for easier configuration via the CLI.
#
# see https://www.keycloak.org/docs/latest/server_admin/index.html#the-admin-cli
#
#########

trap 'exit' ERR

# get the id of the client for a given clientId
getClient () {
    # arguments
    REALM=$1
    CLIENT_ID=$2
    #
    ID=$($KCADM get clients -r $REALM --fields id,clientId | jq '.[] | select(.clientId==("'$CLIENT_ID'")) | .id')
    echo $(sed -e 's/"//g' <<< $ID)
}