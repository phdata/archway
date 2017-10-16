#!/bin/sh

set -x

chmod 777 /etc/krb5.conf
chmod -R 777 /etc/krb5kdc

sed -i "s/#LDAP_BASE_DN#/$LDAP_BASE_DN/" /etc/krb5.conf
sed -i "s/#LDAP_BIND_DN#/$LDAP_BIND_DN/" /etc/krb5.conf
sed -i "s/#LDAP_SERVICE_HOST#/$LDAP_SERVICE_HOST/" /etc/krb5.conf
sed -i "s/#LDAP_SERVICE_PORT#/$LDAP_SERVICE_PORT/" /etc/krb5.conf
sed -i "s/#KERBEROS_KDC_SERVICE_HOST#/${KERBEROS_KDC_SERVICE_HOST?'localhost'}/" /etc/krb5.conf
sed -i "s/#KERBEROS_KDC_SERVICE_PORT#/${KERBEROS_KDC_SERVICE_PORT?88}/" /etc/krb5.conf
sed -i "s/#KERBEROS_ADMIN_SERVICE_HOST#/${KERBEROS_ADMIN_SERVICE_HOST?'localhost'}/" /etc/krb5.conf
sed -i "s/#KERBEROS_ADMIN_SERVICE_PORT#/${KERBEROS_ADMIN_SERVICE_PORT?88}/" /etc/krb5.conf

echo $LDAP_CRED_KEY > /etc/krb5kdc/service.keyfile

chmod 644 /etc/krb5kdc/service.keyfile

cp -f /tmp/kdc.conf /etc/krb5kdc/kdc.conf
