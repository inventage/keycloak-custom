#!/usr/bin/env bash

runKeycloakConfiguration() {
    if [ $KEYCLOAK_USER ] && [ $KEYCLOAK_PASSWORD ]; then
        echo "======================================="
        echo "Waiting for Keycloak to start before keycloak-configuration.sh is executed."
        echo "======================================="

        $JBOSS_HOME/bin/wait-for-it.sh localhost:8080
        $JBOSS_HOME/bin/keycloak-configuration.sh
    else
        echo "skipping keycloak-configuration.sh execution because no user credentials available (KEYCLOAK_USER/KEYCLOAK_PASSWORD)."
    fi
}

if [ $KEYCLOAK_USER ] && [ $KEYCLOAK_PASSWORD ]; then
    $JBOSS_HOME/bin/add-user-keycloak.sh --user $KEYCLOAK_USER --password $KEYCLOAK_PASSWORD
fi

$JBOSS_HOME/bin/standalone.sh -b 0.0.0.0 -c standalone-ha.xml -Djboss.node.name=$WILDFLY_NODE_NAME -Djboss.socket.binding.port-offset=$WILDFLY_NODE_OFFSET $STANDALONE_CUSTOM_OPTIONS &
export KEYCLOAK_PID=$!

runKeycloakConfiguration

trap "kill -HUP  $KEYCLOAK_PID" HUP
trap "kill -TERM $KEYCLOAK_PID" INT
trap "kill -QUIT $KEYCLOAK_PID" QUIT
trap "kill -PIPE $KEYCLOAK_PID" PIPE
trap "kill -TERM $KEYCLOAK_PID" TERM

#to keep the container running until keycloak shuts down
wait $KEYCLOAK_PID
