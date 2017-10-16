#!/bin/sh

set -x

/usr/bin/conf_setup.sh

kdb5_ldap_util -D ${LDAP_BIND_DN} -w ${LDAP_BIND_PASSWORD} create -subtrees ${LDAP_BASE_DN} -r JOTUNN.IO -P ${LDAP_BIND_PASSWORD} -s -H ldap://${LDAP_SERVICE_HOST}:${LDAP_SERVICE_PORT}

exec "$@"
