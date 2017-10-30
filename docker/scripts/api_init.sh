#!/bin/bash

set -x

sed -i "s/#KERBEROS_SERVICE_HOST#/$KERBEROS_SERVICE_HOST/" /etc/krb5.conf

exec "$@"