#! /bin/sh
USAGE="Usage: `basename $0` [-h] [-o okapi_url ] [-u username] [-p password] [-t tenant]"

# https://folio-snapshot-okapi.dev.folio.org/bl-users/login?expandPermissions=true&fullPermissions=true

TENANT="diku"
UN="diku_admin"
PW="admin"
OKAPI="https://folio-snapshot-okapi.dev.folio.org"

# Parse command line options.
while getopts "ho:u:p:t:" OPT; do
  case "$OPT" in
    h)
      echo $USAGE
      exit 0
      ;;
    o)
      OKAPI=$OPTARG
      ;;
    u)
      UN=$OPTARG
      ;;
    p)
      PW=$OPTARG
      ;;
    t)
      TENANT=$OPTARG
      ;;
    \?)
      # getopts issues an error message
      echo $USAGE >&2
      exit 1
      ;;
  esac
done

# Remove the options we parsed above.
shift `expr $OPTIND - 1`

# authn version
# AUTH_TOKEN=$(curl -sSL -D - -X POST -H 'accept: application/json' -H 'Content-type: application/json' \
#     -H "X-Okapi-Tenant: $TENANT" --connect-timeout 5 --max-time 30 -d "{ \"username\":\"${UN}\", \"password\": \"${PW}\" }" \
#     "${OKAPI}/authn/login" | grep -Fi x-okapi-token | sed -r 's/^.*\:\s*(([A-Za-z0-9+\/]+\.){2}[A-Za-z0-9+\/]+)/\1/' | xargs)

# bl-users variant
AUTH_TOKEN=$(curl -sSL -D - -X POST -H 'accept: application/json' -H 'Content-type: application/json' \
    -H "X-Okapi-Tenant: $TENANT" --connect-timeout 5 --max-time 30 -d "{ \"username\":\"${UN}\", \"password\": \"${PW}\" }" \
    "${OKAPI}/bl-users/login?expandPermissions=true&fullPermissions=true" | grep -Fi x-okapi-token | sed -r 's/^.*\:\s*(([A-Za-z0-9+\/]+\.){2}[A-Za-z0-9+\/]+)/\1/' | xargs)
echo $AUTH_TOKEN

exit 0

