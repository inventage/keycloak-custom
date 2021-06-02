#!/usr/bin/env bash

##########
#
# This script contains function for easier configuration via the CLI.
#
# see https://www.keycloak.org/docs/latest/server_admin/index.html#the-admin-cli
#
#########

trap 'exit' ERR

createUUID() {
    # arguments
    NAMESPACE_UUID=$1
    NAME=$2
    #
    echo $(python -c "import uuid; namespace=uuid.UUID('$NAMESPACE_UUID'); generatedUuid=uuid.uuid5(namespace, '$NAME'); print(generatedUuid);")
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

# get the id of the component for the given name
getComponent() {
    # arguments
    REALM=$1
    COMPONENT_NAME=$2
    #
    ID=$($KCADM get components -r $REALM --fields id,name | jq '.[] | select(.name==("'$COMPONENT_NAME'")) | .id')
    echo $(sed -e 's/"//g' <<< $ID)
}

# get the name of the component for the given id
getComponentNameById() {
    # arguments
    REALM=$1
    COMPONENT_ID=$2
    #
    ID=$($KCADM get components -r $REALM --fields id,name | jq '.[] | select(.id==("'$COMPONENT_ID'")) | .name')
    echo $(sed -e 's/"//g' <<< $ID)
}

getTopLevelFlow() {
    # arguments
    REALM=$1
    ALIAS=$2
    #
    ID=$($KCADM get authentication/flows -r $REALM --fields id,alias| jq '.[] | select(.alias==("'$ALIAS'")) | .id')
    echo $(sed -e 's/"//g' <<< $ID)
}

getFlowExecution() {
    # arguments
    REALM=$1
    TOPLEVEL=$2
    FLOW_ID=$3
    #
    ID=$($KCADM get authentication/flows/$TOPLEVEL/executions -r $REALM --fields id,flowId,alias | jq '.[] | select(.flowId==("'$FLOW_ID'")) | .id')
    echo $(sed -e 's/"//g' <<< $ID)
}

getRole() {
    # arguments
    REALM=$1
    CLIENT=$2
    ROLE_NAME=$3
    #
    ROLE=$($KCADM get-roles -r $REALM --cid $CLIENT | jq '.[] | select(.name==("'$ROLE_NAME'")) | .id' )
    echo $(sed -e 's/"//g' <<< $ROLE)
}

getUser() {
    # arguments
    REALM=$1
    USERNAME=$2
    #
    USER=$($KCADM get users -r $REALM -q username=$USERNAME | jq '.[] | select(.username==("'$USERNAME'")) | .id' )
    echo $(sed -e 's/"//g' <<< $USER)
}

# get the id of the protocol mapper
getProtocolMapper () {
    # arguments
    REALM=$1
    CLIENT_ID=$2
    NAME=$3
    #
    PROTOCOLMAPPER=$($KCADM get clients/$CLIENT_ID/protocol-mappers/models -r $REALM --fields id,name | jq '.[] | select(.name==("'$NAME'")) | .id')
    echo $(sed -e 's/"//g' <<< $PROTOCOLMAPPER)
}

getRealm() {
    # arguments
    REALM_NAME=$1
    #
    EXISTING_REALM=$($KCADM get realms/$REALM_NAME | jq '. | .id')
    echo $(sed -e 's/"//g' <<< $EXISTING_REALM)
}

#
createRealm() {
    # arguments
    REALM_NAME=$1
    #
    ID=$(getRealm $REALM_NAME)
    if [ "$ID" == "" ]; then
        $KCADM create realms -s realm="${REALM_NAME}" -s enabled=true
    fi
    echo $(getRealm $REALM_NAME)
}

deleteRealm() {
    # arguments
    REALM_NAME=$1
    #
    EXISTING_REALM=$($KCADM get realms/$REALM_NAME)
    if [ "$EXISTING_REALM" != "" ]; then
        $KCADM delete realms/"$REALM_NAME"
        echo "realm $REALM_NAME deleted"
    fi

}

#
createKey() {
    # arguments
    REALM_NAME=$1
    REALM_ID=$2
    KEY_NAME=$3
    KEYSTORE_FILE=$4
    KEYSTORE_PASSWORD=$5
    KEYSTORE_KEY_ALIAS=$6
    KEYSTORE_KEY_PASSWORD=$7
    KEYSTORE_ACTIVE=$8
    KEYSTORE_PRIORITY=$9
    #
    EXISTING_KEY=$(getKeyByName $REALM_NAME $KEY_NAME)
    if [ "$EXISTING_KEY" == "" ]; then
        $KCADM create components -r $REALM_NAME \
        -s parentId=$REALM_ID \
        -s name=$KEY_NAME \
        -s providerId=java-keystore \
        -s providerType=org.keycloak.keys.KeyProvider \
        -s 'config.keystore=["'"${KEYSTORE_FILE}"'"]' \
        -s 'config.keystorePassword=["'"${KEYSTORE_PASSWORD}"'"]' \
        -s 'config.keyAlias=["'"${KEYSTORE_KEY_ALIAS}"'"]' \
        -s 'config.keyPassword=["'"${KEYSTORE_KEY_PASSWORD}"'"]' \
        -s 'config.active=["'"${KEYSTORE_ACTIVE}"'"]' \
        -s 'config.priority=["'"${KEYSTORE_PRIORITY}"'"]' \
        -s 'config.enabled=["true"]'
    fi
    echo $(getKeyByName $REALM_NAME $KEY_NAME)
}

# get a key for the given name
getKeyByName() {
    # arguments
    REALM_NAME=$1
    KEY_NAME=$2
    #
    KEYS=$($KCADM get keys -r $REALM_NAME | jq '.keys[] | .providerId')
    for KEY in $KEYS
    do
      KEY_ID=$(sed -e 's/"//g' <<< $KEY)
      COMPONENT_NAME=$(getComponentNameById $REALM_NAME $KEY_ID)
      if [[ "$COMPONENT_NAME" == "$KEY_NAME" ]]; then
        echo $KEY_ID
        break
      fi
    done
}

#
createClientScope() {
    # arguments
    REALM_NAME=$1
    NAME=$2
    PROTOCOL=$3
    #
    ID=$(getClientScope $REALM_NAME $NAME)
    if [[ "$ID" == "" ]]; then
      $KCADM create client-scopes -r $REALM_NAME -s name=$NAME -s protocol=$PROTOCOL
    fi
    echo $(getClientScope $REALM_NAME $NAME)
}

#
getClientScope() {
    # arguments
    REALM_NAME=$1
    NAME=$2
    ID=$($KCADM get client-scopes -r $REALM_NAME --fields id,name | jq '.[] | select(.name==("'$NAME'")) | .id')
    echo $(sed -e 's/"//g' <<< $ID)
}

# removes a client scope from the assigned default entry (UI "Assigned Default Client Scopes")
removeDefaultClientScope() {
    # arguments
    REALM_NAME=$1
    CLIENT_SCOPE_NAME=$2
    #
    ID=$(getClientScope $REALM_NAME ${CLIENT_SCOPE_NAME})
    if [[ "$ID" != "" ]]; then
      $KCADM delete default-default-client-scopes/${ID} -r $REALM_NAME
    fi
}

# a protocol mapper is attached to a client for customizing the created JWT for this client
createClientScopeProtocolMapper() {
    # arguments
    REALM_NAME=$1
    CLIENT_SCOPE=$2
    NAME=$3
    PROTOCOL=$4
    MAPPER=$5
    #
    OBJ=$(getClientScopeProtocolMapper $REALM_NAME $CLIENT_SCOPE $NAME)
    if [ "$OBJ" == "" ]; then
        $KCADM create client-scopes/$CLIENT_SCOPE/protocol-mappers/models -r $REALM_NAME -s name=$NAME -s protocol=$PROTOCOL -s protocolMapper=$MAPPER
        OBJ=$(getClientScopeProtocolMapper $REALM_NAME $CLIENT_SCOPE $NAME)
    fi
    echo $OBJ
}

# get the id of the protocol mapper
getClientScopeProtocolMapper () {
    # arguments
    REALM_NAME=$1
    CLIENT_SCOPE=$2
    NAME=$3
    #
    PROTOCOLMAPPER=$($KCADM get client-scopes/$CLIENT_SCOPE/protocol-mappers/models -r $REALM_NAME --fields id,name | jq '.[] | select(.name==("'$NAME'")) | .id')
    echo $(sed -e 's/"//g' <<< $PROTOCOLMAPPER)
}

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

# a protocol mapper is attached to a client for customizing the created JWT for this client
createProtocolMapper() {
    # arguments
    REALM=$1
    CLIENT_ID=$2
    NAME=$3
    PROTOCOL=$4
    MAPPER=$5
    #
    OBJ=$(getProtocolMapper $REALM $CLIENT_ID $NAME)
    if [ "$OBJ" == "" ]; then
        $KCADM create clients/$CLIENT_ID/protocol-mappers/models -r $REALM -s name=$NAME -s protocol=$PROTOCOL -s protocolMapper=$MAPPER
        OBJ=$(getProtocolMapper $REALM $CLIENT_ID $NAME)
    fi
    echo $OBJ
}

createUser() {
    # arguments
    REALM=$1
    USER_NAME=$2
    #
    USER_ID=$(getUser $REALM $USER_NAME)
    if [ "$USER_ID" == "" ]; then
        $KCADM create users -r $REALM -s username=$USER_NAME -s enabled=true
    fi
    echo $(getUser $REALM $USER_NAME)
}

createRole() {
    # arguments
    REALM=$1
    CLIENT=$2
    ROLE_NAME=$3
    ROLE_DESCRIPTION=$4
    #
    ROLE_ID=$(getRole $REALM $CLIENT $ROLE_NAME)
    if [ "$ROLE_ID" == "" ]; then
        $KCADM create clients/$CLIENT/roles -r $REALM -s name=$ROLE_NAME -s description="'$ROLE_DESCRIPTION'"
        echo $(getRole $REALM $CLIENT_ID $ROLE_NAME)
    fi
}

addRoleToRole() {
    # arguments
    REALM_NAME=$1
    CLIENT_ID=$2
    COMPOSITE_ROLE_NAME=$3
    CHILD_CLIENT_NAME=$4
    CHILD_ROLE_NAME=$5
    #
    ROLE_ID=$(getRole $REALM_NAME $CLIENT_ID $COMPOSITE_ROLE_NAME)
    if [ "$ROLE_ID" != "" ]; then
        $KCADM add-roles -r $REALM_NAME --rid $ROLE_ID --cclientid $CHILD_CLIENT_NAME --rolename $CHILD_ROLE_NAME
    fi
}
createTopLevelFlow() {
    # arguments
    REALM=$1
    ALIAS=$2
    #
    FLOW_ID=$(getTopLevelFlow $REALM $ALIAS)
    if [ "$FLOW_ID" == "" ]; then
        $KCADM create authentication/flows -r $REALM -s alias=$ALIAS -s providerId=basic-flow -s topLevel=true -s builtIn=false
    fi
    echo $(getTopLevelFlow $REALM $ALIAS)
}

deleteTopLevelFlow() {
    # arguments
    REALM=$1
    ALIAS=$2
    #
    FLOW_ID=$(getTopLevelFlow $REALM $ALIAS)
    if [ "$FLOW_ID" != "" ]; then
        $KCADM delete authentication/flows/$FLOW_ID -r $REALM
    fi
    echo $(getTopLevelFlow $REALM $ALIAS)
}

# create a new subflow
createSubflow() {
    # arguments
    REALM=$1
    TOPLEVEL=$2
    PARENT=$3
    ALIAS=$4
    REQUIREMENT=$5
    #
    FLOW_ID=$($KCADM create authentication/flows/$PARENT/executions/flow -i -r $REALM -b '{"alias" : "'$ALIAS'" , "type" : "basic-flow"}')
    EXECUTION_ID=$(getFlowExecution $REALM $TOPLEVEL $FLOW_ID)
    $KCADM update authentication/flows/$TOPLEVEL/executions -r $REALM -b '{"id":"'$EXECUTION_ID'","requirement":"'$REQUIREMENT'"}'
    echo "Created new subflow with id '$FLOW_ID', alias '$ALIAS'"
}

# create a new execution for a given providerId (the providerId is defined by AuthenticatorFactory)
createExecution() {
    # arguments
    REALM=$1
    FLOW=$2
    PROVIDER=$3
    REQUIREMENT=$4
    #
    EXECUTION_ID=$($KCADM create authentication/flows/$FLOW/executions/execution -i -b '{"provider" : "'$PROVIDER'"}' -r $REALM)
    $KCADM update authentication/flows/$FLOW/executions -b '{"id":"'$EXECUTION_ID'","requirement":"'$REQUIREMENT'"}' -r $REALM
    echo "Created new execution '$PROVIDER' with id $EXECUTION_ID"
}

deleteAllMappers() {
    # arguments
    realm=$1
    clientId=$2
    #
    MAPPER_IDS_JSON=$($KCADM get clients/$clientId/protocol-mappers/models -r $realm --fields 'id')
    MAPPER_IDS_RAW=$(echo $MAPPER_IDS_JSON | jq -r '.[] | .id')
    read -r -a MAPPER_IDS <<< ${MAPPER_IDS_RAW[@]}
    for mapperId in "${MAPPER_IDS[@]}";
    do
        $KCADM delete clients/$clientId/protocol-mappers/models/$mapperId -r $realm
    done
}

# remove the given client scope from the default client scope list
removeDefaultClientScope() {
    # arguments
    REALM=$1
    CLIENT_SCOPE_NAME=$2
    #
    CLIENT_SCOPE_ID=$(sed -e 's/"//g' <<<  $($KCADM get client-scopes/ -r $REALM --fields id,name | jq '.[] | select(.name==("'$CLIENT_SCOPE_NAME'")) | .id'))
    $KCADM delete default-default-client-scopes/$CLIENT_SCOPE_ID -r $REALM

    echo "client scope $CLIENT_SCOPE_NAME ($CLIENT_SCOPE_ID) removed as default"
}

createIdentityProvider() {
    # arguments
    REALM_NAME=$1
    ALIAS=$2
    NAME=$3
    PROVIDER_ID=$4
    #
    IDENTITY_PROVIDER_ID=$(getIdentityProviderId $REALM_NAME $ALIAS)
    if [ "$IDENTITY_PROVIDER_ID" == "" ]; then
        $KCADM create identity-provider/instances -r $REALM_NAME -s alias=$ALIAS -s displayName="$NAME" -s providerId=$PROVIDER_ID
    fi
    echo $(getIdentityProviderId $REALM_NAME $ALIAS)
}

# get the id of the identityProvider with the given alias
getIdentityProviderId () {
    # arguments
    REALM=$1
    IDP_ALIAS=$2
    #
    ID=$($KCADM get identity-provider/instances -r $REALM --fields alias,internalId | jq '.[] | select(.alias==("'$IDP_ALIAS'")) | .internalId')
    echo $(sed -e 's/"//g' <<< $ID)
}

deleteIdentityProvider() {
    # arguments
    REALM_NAME=$1
    ALIAS=$2
    #
    IDENTITY_PROVIDER_ID=$(getIdentityProviderId $REALM_NAME $ALIAS)
    if [ "$IDENTITY_PROVIDER_ID" != "" ]; then
        $KCADM delete identity-provider/instances/$IDENTITY_PROVIDER_ID -r $REALM_NAME
    fi
}
getGroup() {
    # arguments
    REALM=$1
    GROUP_NAME=$2
    #
    GROUP_ID=$($KCADM get groups -r $REALM | jq '.[] | select(.name==("'$GROUP_NAME'")) | .id' )
    echo $(sed -e 's/"//g' <<< $GROUP_ID)
}

createGroup() {
    # arguments
    REALM=$1
    GROUP_NAME=$2
    #
    GROUP_ID=$(getGroup $REALM $GROUP_NAME)
    if [ "$GROUP_ID" == "" ]; then
        $KCADM create groups -r $REALM -s name=$GROUP_NAME
    fi
    echo $(getGroup $REALM $GROUP_NAME)
}

getRealmRole() {
    # arguments
    REALM=$1
    ROLE_NAME=$2
    #
    ROLE=$($KCADM get roles -r $REALM | jq '.[] | select(.name==("'$ROLE_NAME'")) | .id' )
    echo $(sed -e 's/"//g' <<< $ROLE)
}

# get the whole realm role as a JSON object
getRealmRoleObject() {
    # arguments
    REALM=$1
    ROLE_NAME=$2
    #
    ROLE=$($KCADM get roles -r $REALM | jq '.[] | select(.name==("'$ROLE_NAME'"))' )
    echo $ROLE
}

createRealmRole() {
    # arguments
    REALM=$1
    ROLE_NAME=$2
    ROLE_DESCRIPTION=$3
    #
    ROLE_ID=$(getRealmRole $REALM $CLIENT $ROLE_NAME)
    if [ "$ROLE_ID" == "" ]; then
        $KCADM create roles -r $REALM -s name=$ROLE_NAME -s description="'$ROLE_DESCRIPTION'"
        echo $(getRealmRole $REALM $CLIENT_ID $ROLE_NAME)
    fi
}

getExecutionId() {
    #arguments
    REALM=$1
    FLOW_ID=$2
    PROVIDER_ID=$3
    EXECUTION_ID=$($KCADM get authentication/flows/$FLOW_ID/executions -r $REALM --fields providerId,id | jq '.[] | select(.providerId==("'$PROVIDER_ID'")) |.id')
    echo $(sed -e 's/"//g' <<< $EXECUTION_ID)
}

createAudienceMapper() {
    #arguments
    REALM=$1
    CLIENT_ID=$2
    CLIENT_NAME=$3

    MAPPER_ID=$($KCADM get clients/$CLIENT_ID/protocol-mappers/models -r $REALM | jq '.[] | select(.name==("'$CLIENT_NAME' audience mapper")) |.id')

    if [ "$MAPPER_ID" == "" ]; then
    $KCADM create clients/$CLIENT_ID/protocol-mappers/models -r $REALM -f - <<EOF
    {
        "name": "$CLIENT_NAME audience mapper",
        "protocol": "openid-connect",
        "protocolMapper": "oidc-audience-mapper",
        "config": {
            "included.client.audience" : "$CLIENT_NAME",
            "id.token.claim" : "true",
            "access.token.claim" : "true"
        }
    }
EOF

    fi


}

registerRequiredAction() {
    #arguments
    REALM_NAME=$1
    PROVIDER_ID=$2
    NAME=$3

    $KCADM delete authentication/required-actions/$PROVIDER_ID -r $REALM_NAME
    $KCADM create authentication/register-required-action -r $REALM_NAME -s providerId="$PROVIDER_ID" -s name="$NAME"
}


# get the id of the LdapStorageProvider with the given name
getStorageProvider() {
    # arguments
    REALM=$1
    NAME=$2
    PROVIDER_ID=$3
    #
    echo $($KCADM get components -r $REALM --fields id,name,providerId,providerType | jq -r '.[] | select(.name==("'$NAME'")) | select(.providerId==("'$PROVIDER_ID'")) | select(.providerType==("org.keycloak.storage.UserStorageProvider")) | .id')
}

createStorageProvider() {
    # arguments
    REALM_NAME=$1
    PROVIDER_NAME=$2
    PROVIDER_ID=$3
    #
    EXISTING_PROVIDER=$(getStorageProvider $REALM_NAME $PROVIDER_NAME $PROVIDER_ID)
    if [ "$EXISTING_PROVIDER" == "" ]; then
         $KCADM create components -r $REALM_NAME \
        -s name=$PROVIDER_NAME \
        -s providerId=$PROVIDER_ID \
        -s providerType=org.keycloak.storage.UserStorageProvider \
        -s 'config.priority=["0"]' \
        -s 'config.enabled=["true"]'
        EXISTING_PROVIDER=$(getStorageProvider $REALM_NAME $PROVIDER_NAME $PROVIDER_ID)
    fi
    echo $EXISTING_PROVIDER
}

# get the id of the LdapStorageProvider with the given name
getLdapStorageProvider() {
    # arguments
    REALM=$1
    NAME=$2
    #
    echo $(getStorageProvider $REALM $NAME ldap)
}

createLdapStorageProvider() {
    # arguments
    REALM_NAME=$1
    PROVIDER_NAME=$2
    #
    echo $(createStorageProvider $REALM_NAME $PROVIDER_NAME ldap)
}

getFormSubflow() {
    # arguments
    REALM_NAME=$1
    TOPLEVEL=$2
    DISPLAY_NAME=$3
    #
    ID=$($KCADM get authentication/flows/$TOPLEVEL/executions -r ${REALM_NAME} --fields id,displayName,flowId | jq '.[] | select(.displayName==("'${DISPLAY_NAME}'")) | .id')
    echo $(sed -e 's/"//g' <<< $ID)
}

# create a new registration subflow
createRegistrationSubflow() {
    # arguments
    REALM_NAME=$1
    PARENT=$2
    DISPLAY_NAME=$3
    REQUIREMENT=$4
    #
    $KCADM create authentication/flows/$PARENT/executions/flow -r ${REALM_NAME} -b '{"alias": "'$DISPLAY_NAME'", "type": "form-flow", "provider": "registration-page-form"}'
    ID=$(getFormSubflow ${REALM_NAME} ${PARENT} ${DISPLAY_NAME})
    $KCADM update authentication/flows/$PARENT/executions -r ${REALM_NAME} -b '{"id":"'$ID'","requirement":"'$REQUIREMENT'"}'
    echo $ID
}