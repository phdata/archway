#!/bin/sh

set -x

/usr/bin/conf_setup.sh

kdb5_ldap_util -D ${LDAP_BIND_DN} -w ${LDAP_BIND_PASSWORD} create -subtrees ${LDAP_BASE_DN} -r JOTUNN.IO -P ${LDAP_BIND_PASSWORD} -s -H ldap://${LDAP_SERVICE_HOST}:${LDAP_SERVICE_PORT}

kadmin.local -q "addprinc -pw admin admin/admin"
echo "*/admin@JOTUNN.IO *" > /etc/krb5kdc/kadm5.acl

exec "$@"
