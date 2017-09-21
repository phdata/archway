FROM osixia/openldap

COPY ./src/test/resources/basicUser.ldif /container/service/slapd/assets/config/bootstrap/ldif/custom/