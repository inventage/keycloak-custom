#!/usr/bin/env bash

trap 'exit' ERR

echo ""
echo "----------------- keycloak_custom_config.sh ---------------"
echo ""

createRealm example2
createClient example2 client1
createUser example2 user1
