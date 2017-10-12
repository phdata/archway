FROM ubuntu:14.04

RUN apt-get update && \
    apt-get install -y krb5-admin-server krb5-kdc-ldap

ADD ./scripts/krb5.conf /etc/krb5.conf
ADD ./scripts/kdc.conf /etc/krb5kdc/kdc.conf
ADD ./scripts/conf_setup.sh /usr/bin/conf_setup.sh
ADD ./scripts/admin_init.sh /usr/bin/admin_init.sh

ENTRYPOINT /usr/bin/admin_init.sh

CMD /usr/sbin/kadmind
