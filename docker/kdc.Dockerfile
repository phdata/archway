FROM ubuntu:artful

RUN apt-get update && \
    apt-get install -y krb5-kdc krb5-kdc-ldap

ADD ./scripts/krb5.conf /etc/krb5.conf
ADD ./scripts/kdc.conf /tmp/kdc.conf

ADD ./scripts/kdc_init.sh /usr/bin/kdc_init.sh
ADD ./scripts/conf_setup.sh /usr/bin/conf_setup.sh

RUN chmod +x /usr/bin/kdc_init.sh & \
    chmod +x /usr/bin/conf_setup.sh

ENTRYPOINT ["/bin/bash", "/usr/bin/kdc_init.sh"]

CMD ["/usr/sbin/krb5kdc", "-n"]
