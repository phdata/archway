#!/bin/bash

/usr/bin/conf_setup.sh

kadmin.local -q "addprinc -pw admin admin/admin"
echo "*/admin@JOTUNN.IO *" > /etc/krb5kdc/kadm5.acl


exec "$@"
