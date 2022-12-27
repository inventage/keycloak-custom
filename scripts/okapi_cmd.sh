#! /bin/sh
USAGE="Usage: `basename $0` [-h] [-u username] [-p password] [-t tenant] uri"

TENANT="diku"
UN="diku_admin"
PW="admin"
OKAPI="https://folio-snapshot-okapi.dev.folio.org"

# examples
# ./okapi_cmd.sh "/users?query=username%3ddevonte"

# Parse command line options.
while getopts hupt: OPT; do
  case "$OPT" in
    h)
      echo $USAGE
      exit 0
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

# We want one non-option argument.
if [ $# -ne 1 ]; then
  echo $USAGE >&2
  exit 1
fi

# Access additional arguments as usual through
# variables $@, $*, $1, $2, etc. or using this loop:
URI=$1

AUTH_TOKEN=$(curl -sSL -D - -X POST -H 'accept: application/json' -H 'Content-type: application/json' \
    -H "X-Okapi-Tenant: $TENANT" --connect-timeout 5 --max-time 30 -d "{ \"username\":\"${UN}\", \"password\": \"${PW}\" }" \
    "${OKAPI}/authn/login" | grep -Fi x-okapi-token | sed -r 's/^.*\:\s*(([A-Za-z0-9+\/]+\.){2}[A-Za-z0-9+\/]+)/\1/' | xargs)

curl -sSL -XGET -H "X-Okapi-Token: ${AUTH_TOKEN}" -H 'accept: application/json' -H 'Content-type: application/json' \
  --http1.1 -H "X-Okapi-Tenant: $TENANT" --connect-timeout 5 --max-time 30 "${OKAPI}${URI}" | jq

exit 0

