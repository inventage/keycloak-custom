#!/usr/bin/env bash

##########
#
# Start Keycloak with the environment variables defined by the 3 .env files.
#
#########

set -euo pipefail

CURRENTDIR=$(dirname "$0")

export $(grep -v '^#' "${CURRENTDIR}"/../docker-compose/src/main/resources/keycloak.common.env | xargs)
export $(grep -v '^#' "${CURRENTDIR}"/../docker-compose/src/main/resources/keycloak.specific.env | xargs)
export $(grep -v '^#' "${CURRENTDIR}"/../docker-compose/src/main/resources/secrets.env | xargs)

# set the working directory as in the docker image
cd "${CURRENTDIR}"/target/keycloak/
bash ./bin/kc.sh --debug start-dev
