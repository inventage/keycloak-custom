#!/usr/bin/env bash

##########
#
# This script contains function for easier configuration via the CLI.
#
# see https://www.keycloak.org/docs/latest/server_admin/index.html#the-admin-cli
#
#########

trap 'exit' ERR

# the new realm is also set as enabled
createRealm() {
    # arguments
    REALM_NAME=$1
    #
    EXISTING_REALM=$($KCADM get realms/$REALM_NAME)
    if [ "$EXISTING_REALM" == "" ]; then
        $KCADM create realms -s realm="${REALM_NAME}" -s enabled=true
    fi
}

# the new client is also set as enabled
createClient() {
    # arguments
    REALM_NAME=$1
    CLIENT_ID=$2
    #
    ID=$(getClient $REALM_NAME $CLIENT_ID)
    if [[ "$ID" == "" ]]; then
        $KCADM create clients -r $REALM_NAME -s clientId=$CLIENT_ID -s enabled=true
    fi
    echo $(getClient $REALM_NAME $CLIENT_ID)
}

# get the id of the client for a given clientId
getClient () {
    # arguments
    REALM=$1
    CLIENT_ID=$2
    #
    ID=$($KCADM get clients -r $REALM --fields id,clientId | jq '.[] | select(.clientId==("'$CLIENT_ID'")) | .id')
    echo $(sed -e 's/"//g' <<< $ID)
}

# create a user for the given username if it doesn't exist yet and return the object id
createUser() {
    # arguments
    REALM_NAME=$1
    USER_NAME=$2
    #
    USER_ID=$(getUser $REALM_NAME $USER_NAME)
    if [ "$USER_ID" == "" ]; then
        $KCADM create users -r $REALM_NAME -s username=$USER_NAME -s enabled=true
    fi
    echo $(getUser $REALM_NAME $USER_NAME)
}

# the object id of the user for a given username
getUser() {
    # arguments
    REALM_NAME=$1
    USERNAME=$2
    #
    USER=$($KCADM get users -r $REALM_NAME -q username=$USERNAME | jq '.[] | select(.username==("'$USERNAME'")) | .id' )
    echo $(sed -e 's/"//g' <<< $USER)
}
