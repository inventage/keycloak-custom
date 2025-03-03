#!/usr/bin/env bash

##########
#
# This script contains function for easier configuration via the CLI.
#
# see https://www.keycloak.org/docs/latest/server_admin/index.html#the-admin-cli
#
#########

trap 'exit' ERR

WAIT_INTERVAL_SECONDS=10

# the new realm is also set as enabled
createRealm() {
    # arguments
    REALM_NAME=$1
    #
    EXISTING_REALM=$($KCADM get realms/$REALM_NAME ${KCADM_CONFIG})
    if [ "$EXISTING_REALM" == "" ]; then
        $KCADM create realms -s realm="${REALM_NAME}" -s enabled=true ${KCADM_CONFIG}
    fi
    waitUntilRealmIsAvailable $REALM_NAME
}

# get the name of the realm for a given name
getRealm () {
    REALM_NAME=$1
    REALM=$($KCADM get realms --fields realm ${KCADM_CONFIG} | jq '.[] | select(.realm==("'$REALM_NAME'")) | .realm')
    echo $(sed -e 's/"//g' <<< $REALM)
}

# wait until realm is available
waitUntilRealmIsAvailable () {
    echo "Wait until realm is available"
    REALM_NAME=$1

    for i in $(seq 0 9); do
      seconds=$((i*WAIT_INTERVAL_SECONDS))
      attempts=$((i + 1))
      echo "Attempt number: $attempts"

      GET_REALM=$(getRealm "$REALM_NAME")

      if [[ $GET_REALM == "$REALM_NAME" ]]; then
        echo "Realm available after $attempts attempt(s)"
        echo "Realm available after $seconds second(s)"
        return
      fi

      # sleep for specified interval
      echo "wait for $WAIT_INTERVAL_SECONDS second(s) before next check"
      sleep $WAIT_INTERVAL_SECONDS

    done

    echo "Creating Realm Failed"
    exit 1
}

# the new client is also set as enabled
createClient() {
    # arguments
    REALM_NAME=$1
    CLIENT_ID=$2
    #
    ID=$(getClient $REALM_NAME $CLIENT_ID)
    if [[ "$ID" == "" ]]; then
        $KCADM create clients -r $REALM_NAME -s clientId=$CLIENT_ID -s enabled=true ${KCADM_CONFIG}
    fi
    echo $(getClient $REALM_NAME $CLIENT_ID)
}

# get the id of the client for a given clientId
getClient () {
    # arguments
    REALM=$1
    CLIENT_ID=$2
    #
    ID=$($KCADM get clients -r $REALM --fields id,clientId ${KCADM_CONFIG} | jq '.[] | select(.clientId==("'$CLIENT_ID'")) | .id')
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
        $KCADM create users -r $REALM_NAME -s username=$USER_NAME -s enabled=true ${KCADM_CONFIG}
    fi
    echo $(getUser $REALM_NAME $USER_NAME)
}

# the object id of the user for a given username
getUser() {
    # arguments
    REALM_NAME=$1
    USERNAME=$2
    #
    USER=$($KCADM get users -r $REALM_NAME -q username=$USERNAME ${KCADM_CONFIG} | jq '.[] | select(.username==("'$USERNAME'")) | .id' )
    echo $(sed -e 's/"//g' <<< $USER)
}
