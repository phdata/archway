FROM osixia/openldap:1.1.10

ENV DEBIAN_FRONTEND=noninteractive

RUN rm -f /etc/dpkg/dpkg.cfg.d/01_nodoc && \
    rm -f /etc/dpkg/dpkg.cfg.d/01_nolocales && \
    rm -f /etc/dpkg/dpkg.cfg.d/docker

RUN apt-get update && apt-get install -y krb5-kdc-ldap && \
    rm -rf /var/lib/apt/lists/* && \
    ls -la /usr/share/doc/krb5-kdc-ldap

RUN gzip -d /usr/share/doc/krb5-kdc-ldap/kerberos.schema.gz && \
    cp /usr/share/doc/krb5-kdc-ldap/kerberos.schema /container/service/slapd/assets/config/bootstrap/schema/

COPY ./ldif/*.ldif /container/service/slapd/assets/config/bootstrap/ldif/custom/
