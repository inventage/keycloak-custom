#!/usr/bin/env bash

##########
#
# Run the setup of Keycloak with the environment variable values from the .env files of the docker-compose module.
#
#########

set -euo pipefail

export $(grep -v '^#' ./docker-compose/src/main/resources/keycloak.common.env | xargs)
export $(grep -v '^#' ./docker-compose/src/main/resources/keycloak.specific.env | xargs)
export $(grep -v '^#' ./docker-compose/src/main/resources/secrets.env | xargs)

# set the working directory as in the docker image
cd ./server/target/keycloak/
bash ./bin/keycloak-setup.sh
