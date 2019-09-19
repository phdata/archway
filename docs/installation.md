# Installation

## Installation with cm_tool

cm_tool is a python library to assist installing CDH services.

To install cm_tool run

```bash

pip install cm_tool --extra-index https://repository.phdata.io/artifactory/api/pypi/python-internal/simple

```

Below is an example installation of Archway using the `service_installer script` in `cm_tool`.

The script assumes the Archway CSD is available and parcel repo has been added to the parcel configuration.

First create a file `cm.properties` with the following format:

```
cluster_name=<cluster name or id>
cluster_cm_host=manager.company.com
cluster_cm_port=7183
cluster_cm_username=<admin username>
cluster_cm_password=<admin password>
cluster_cm_tls=true
```

Save this script as `archway-service.yml` and run the command:

```bash

service_installer archway-service.yml
```

```yaml
service:
  type: ARCHWAY
  parcel:
    product: ARCHWAY
    version: 2.1.1
  name: Archway
  start: true
  dependencies: # service_installer will automatically pick up Hive, Sentry, and Impala dependencies
    - type: IMPALA
      parameter: impala_service
    - type: SENTRY
      parameter: sentry_service
    - type: HIVE
      parameter: hive_service
  parameters:
    - name: archway.rest.port
      value: 8080
  roles:
    - type: ARCHWAY_SERVER
      name: ARCHWAY_SERVER
      hosts:
        - host.company.com # Modify this to point at the host Archway will be installed on
      parameters:
        - name: archway.cluster.url
          value: https://manager.company.com:7183 # Modify to point at cloudera manager
        - name: bundle.token
          value: ''
        - name: archway.db.hive.url
          value: jdbc:hive2://master2.company.com:10000/default;ssl=true;principal=hive/_HOST@COMPANY.COM
        - name: archway.db.impala.url
          value: jdbc:impala://worker1.company.com:21050;SSL=1;AuthMech=1;SSLTrustStorePwd=changeit;SSLTrustStore=/opt/cloudera/security/pki/jks/jssecacerts-java-1.8.jks;CAIssuedCertNamesMismatch=1;AllowSelfSignedCerts=1;KrbHostFQDN=worker1.valhalla.phdata.io;KrbServiceName=impala;KrbAuthType=0;KrbRealm=COMPANY.COM
        - name: archway.db.meta.url
          value: jdbc:mysql://metastore.company.com:3306/heimdali
        - name: archway.featureFlags
          value: messaging,application,workspace-delete
        - name: archway.templates.templateRoot
          value: /opt/phdata/archway/templates
        - name: archway.approvers.infrastructure
          value: cn=edh_admin_full,ou=groups,ou=Hadoop,dc=company,dc=com
        - name: archway.approvers.notificationEmail
          value: approvers@company.com
        - name: archway.approvers.risk
          value: cn=edh_admin_full,ou=groups,ou=Hadoop,dc=company,dc=com
        - name: archway.cluster.admin.username
          value: <CM admin user>
        - name: archway.cluster.admin.password
          value: <CM admin user password>
        - name: archway.cluster.environment
          value: Dev
        - name: archway.cluster.name
          value: cluster
        - name: archway.cluster.nameservice
          value: valhalla
        - name: archway.cluster.sessionRefresh
          value: 1h
        - name: archway.db.impala.driver
          value: com.cloudera.impala.jdbc41.Driver
        - name: archway.db.meta.driver
          value: com.mysql.jdbc.Driver
        - name: archway.db.meta.password
          value: <database password>
        - name: archway.db.meta.username
          value: admin
        - name: archway.kafka.zookeeperConnect
          value: master2.company.com:2181,master1.company.com:2181,master3.company.com:2181
        - name: archway.kafka.secureTopics
          value: false
        - name: archway.ldap.provisioningBinding.server
          value: ad1.company.com
        - name: archway.ldap.provisioningBinding.port
          value: 636
        - name: archway.ldap.provisioningBinding.bindDN
          value: CN=admin,CN=Users,DC=company,DC=com
        - name: archway.ldap.provisioningBinding.bindPassword
          value: PhdataAD888!
        - name: archway.ldap.lookupBinding.server
          value: ad1.company.com
        - name: archway.ldap.lookupBinding.port
          value: 636
        - name: archway.ldap.lookupBinding.bindDN
          value: CN=admin,CN=Users,DC=company,DC=com
        - name: archway.ldap.lookupBinding.bindPassword
          value: <password for ad server>
        - name: archway.ldap.baseDN
          value: DC=company,DC=com
        - name: archway.ldap.domain
          value: COMPANY.COM
        - name: archway.ldap.groupPath
          value: ou=groups,ou=Archway,DC=company,DC=com
        - name: archway.ldap.realm
          value: phdata
        - name: archway.rest.secret
          value: <insert a random value here>
        - name: archway.rest.authType
          value: ldap
        - name: archway.java.truststore.location
          value: /opt/cloudera/security/pki/jks/jssecacerts-java-1.8.jks
        - name: archway.java.truststore.password
          value: changeit
        - name: ssl_server_keystore_keypassword
          value: <key password>
        - name: ssl_server_keystore_location
          value: /opt/cloudera/security/pki/jks/local.jks
        - name: ssl_server_keystore_password
          value: <key password>
        - name: ssl_enabled
          value: true
        - name: archway.smtp.auth
          value: true
        - name: archway.smtp.fromEmail
          value: cluster@company.com
        - name: archway.smtp.host
          value: <smtp host>
        - name: archway.smtp.pass
          value: <smtp password>
        - name: archway.smtp.port
          value: 587
        - name: archway.smtp.ssl
          value: true
        - name: archway.smtp.user
          value: <smtp user>
        - name: archway.workspaces.dataset.defaultCores
          value: 25
        - name: archway.workspaces.dataset.defaultMemory
          value: 100
        - name: archway.workspaces.dataset.defaultSize
          value: 4000
        - name: archway.workspaces.dataset.poolParents
          value: root
        - name: archway.workspaces.sharedWorkspace.defaultCores
          value: 25
        - name: archway.workspaces.sharedWorkspace.defaultMemory
          value: 100
        - name: archway.workspaces.sharedWorkspace.defaultSize
          value: 1000
        - name: archway.workspaces.sharedWorkspace.poolParents
          value: root
        - name: archway.workspaces.user.defaultCores
          value: 5
        - name: archway.workspaces.user.defaultMemory
          value: 20
        - name: archway.workspaces.user.defaultSize
          value: 250
        - name: archway.workspaces.user.poolParents
          value: root.users
```
