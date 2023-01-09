
# Get the keycloak response and extract the access token
KC_RESPONSE=`curl -H "Content-Type: application/x-www-form-urlencoded" -d 'client_secret=xzMZwpWXBJeyKXSsr3L5WHJ6cyp0qUZO' -d 'client_id=dcb' -d 'username=123456RG' -d 'password=1234' -d 'grant_type=password' 'https://reshare-hub-kc.libsdev.k-int.com/realms/sierra-kc-towers/protocol/openid-connect/token' | jq -r .access_token`

# Decode access token into JWT
echo $KC_RESPONSE | jq -R 'split(".") | .[0],.[1] | @base64d | fromjson'
