#!/bin/bash

kdb5_ldap_util -D ${LDAP_BIND_DN} -w ${LDAP_BIND_PASSWORD} create -subtrees ${LDAP_BASE_DN} -r JOTUNN.IO -P ${LDAP_BIND_PASSWORD} -s -H ldap://${LDAP_SERVICE_HOST}:${LDAP_SERVICE_PORT} ${LDAP_BIND_USER}

kdb5_ldap_util -D ${LDAP_BIND_DN} -w ${LDAP_BIND_PASSWORD} stashsrvpw -f /etc/krb5kdc/service.keyfile ${LDAP_BIND_USER}

exec $@
