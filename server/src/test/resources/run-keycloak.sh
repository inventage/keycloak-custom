#!/usr/bin/env bash

##########
#
# This script takes properties from additional files and sets them as environment variables (for simulating docker-compose with 'env_file' entries)
# before executing the shell script given by the first argument.
# The current directory must be `keycloak-server/target/keycloak` (as the WORKDIR in the docker image)
# Example: run-keycloak.sh keycloak-configuration.sh UAT.env specific.env
#
#########

set -euo pipefail

echo "$@"

# first argument is the command for the kc.sh script
COMMAND=$1
shift

while [[ $# -gt 0 ]]
do
    CONF_FILE=$1
    shift
    if [[ -f $CONF_FILE ]]
    then
        echo "Loading file $CONF_FILE"
        export $(grep -v '^#' $CONF_FILE | xargs)
    else
        echo "Env file $CONF_FILE does not exist"
    fi
done

# set the working directory as in the docker image
cd ./server/target/keycloak/
bash ./bin/kc.sh --debug $COMMAND
