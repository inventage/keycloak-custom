
# Get the keycloak response and extract the access token
KC_RESPONSE=`curl -H "Content-Type: application/x-www-form-urlencoded" -d 'client_secret=Dk2AXWCPIqsryd7Jxa80s2sr2zxrMICy' -d 'client_id=dcb' -d 'username=diku_admin' -d 'password=admin' -d 'grant_type=password' 'https://reshare-hub-kc.libsdev.k-int.com/realms/folio-snapshot/protocol/openid-connect/token' | jq -r .access_token`

# Decode access token into JWT
echo $KC_RESPONSE | jq -R 'split(".") | .[0],.[1] | @base64d | fromjson'
