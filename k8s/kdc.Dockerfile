FROM ubuntu:14.04

RUN apt-get update && \
    apt-get install -y krb5-kdc krb5-kdc-ldap

ADD ./scripts/krb5.conf /etc/krb5.conf
ADD ./scripts/kdc.conf /etc/krb5kdc/kdc.conf
ADD ./scripts/kdc_init.sh /usr/bin/

RUN chmod +x /usr/bin/kdc_init.sh

ENTRYPOINT /usr/bin/kdc_init.sh

CMD krb5-kdc