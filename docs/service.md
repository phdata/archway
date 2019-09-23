## Service Installation Parameters

Description: The Archway Service

### Parameters

**archway.rest.port**

- Label: REST API Port
- Description: Archway REST API Port
- Required: true
- Default: 8080

**bundle.token**

- Label: Bundle Token
- Description: Artifactory access token
- Required: false
- Default:

### Roles

- Service: ARCHWAY_SERVER
- Label: Archway Server

#### Parameters

**archway.rest.secret**

- Label: REST API Secret
- Description: Archway REST API Secret for hashing tokens. This parameter should be set to a randomly generated string.
- Required: true
- Default:

**archway.rest.authType**

- Label: REST Authentication Type
- Description: Valid auth types are 'ldap' and 'spnego'
- Required: true
- Default: ldap

**archway.cluster.sessionRefresh**

- Label: Kerberos Refresh Interval
- Description: Interval between refreshing kerberos cache (eg. 1h, 30m, etc)
- Required: true
- Default: 1h

**archway.cluster.url**

- Label: Cloudera Manager Base URL
- Description: The base URL for CM API (eg. https://manager.valhalla.phdata.io:7183)
- Required: true
- Default:

**archway.cluster.admin.username**

- Label: Archway CM User
- Description: The username for the CM user
- Required: true
- Default:

**archway.cluster.admin.password**

- Label: Archway CM Password
- Description: The password for the CM user
- Required: true
- Default:

**archway.cluster.name**

- Label: Cluster Name
- Description: The name of the cluster to manage
- Required: true
- Default:

**archway.cluster.nameservice**

- Label: HDFS nameservice
- Description: The HDFS nameservice (dfs.nameservice)
- Required: false
- Default:

**archway.cluster.environment**

- Label: Cluster Environment
- Description: The environment the cluster represents, such as dev, qa, prod, etc. Should be lowercase, no spaces or special chars
- Required: true
- Default:

**archway.additional.classpath**

- Label: Additional Classpath
- Description: Classpath values to be appended to the Archway API classpath
- Required: false
- Default:

**archway.cluster.hueOverride.host**

- Label: Hue Override Host
- Description: Optionally specify the host for Hue's alternative location
- Required: false
- Default:

**archway.cluster.hueOverride.port**

- Label: Hue Override Port
- Description: Optionally specify the port for Hue's alternative location
- Required: false
- Default:

**archway.cluster.beeswaxPort**

- Label: Impala Beeswax Port
- Description: The port for Impala's beeswax port used by impala-shell
- Required: false
- Default:

**archway.cluster.hiveServer2Port**

- Label: Impala HiveServer2 Port
- Description: The port for Impala's hiveserver2 port used by clients
- Required: false
- Default:

**archway.workspaces.user.defaultSize**

- Label: Default User Workspace Quota Size
- Description: The default size for user workspaces
- Required: true
- Default: 250

**archway.workspaces.user.defaultCores**

- Label: Default User YARN Cores
- Description: The default user workspace number of cores
- Required: true
- Default: 5

**archway.workspaces.user.defaultMemory**

- Label: User Default YARN Memory
- Description: Default amount of memory for user workspaces
- Required: true
- Default: 20

**archway.workspaces.user.poolParents**

- Label: User YARN Pool Parents
- Description: Default parent pools for user workspaces (eg. "root")
- Required: true
- Default: root.users

**archway.workspaces.sharedWorkspace.defaultSize**

- Label: Default Shared Workspace Quota Size
- Description: The default size for shared workspaces
- Required: true
- Default: 1000

**archway.workspaces.sharedWorkspace.defaultCores**

- Label: Default Shared YARN Cores
- Description: The default shared workspace number of cores
- Required: true
- Default: 25

**archway.workspaces.sharedWorkspace.defaultMemory**

- Label: Shared Default YARN Memory
- Description: Default amount of memory for shared workspaces
- Required: true
- Default: 100

**archway.workspaces.sharedWorkspace.poolParents**

- Label: Shared YARN Pool Parents
- Description: Default parent pools for shared workspaces (eg. "root"
- Required: true
- Default: root

**archway.workspaces.dataset.defaultSize**

- Label: Default Governed Dataset Quota Size
- Description: The default size for governed datasets
- Required: true
- Default: 4000

**archway.workspaces.dataset.defaultCores**

- Label: Default Dataset YARN Cores
- Description: The default dataset workspace number of cores
- Required: true
- Default: 25

**archway.workspaces.dataset.defaultMemory**

- Label: Datase Default YARN Memory
- Description: Default amount of memory for dataset workspaces
- Required: true
- Default: 100

**archway.workspaces.dataset.poolParents**

- Label: Dataset YARN Pool Parents
- Description: Default parent pools for dataset workspaces (eg. "root"
- Required: true
- Default: root

**archway.db.meta.driver**

- Label: db_driver
- Description: The database driver to use for storage
- Required: true
- Default:

**archway.db.meta.url**

- Label: JDBC string
- Description: The JDBC string to the Archway database
- Required: true
- Default:

**archway.db.meta.username**

- Label: Database Username
- Description: User with insert permissions
- Required: true
- Default:

**archway.db.meta.password**

- Label: Database Password
- Description: Password for the user with insert permissions
- Required: true
- Default:

**archway.db.hive.url**

- Label: Hive URL
- Description: The URL for connecting to Hive
- Required: true
- Default:

**archway.db.impala.url**

- Label: Impala connection string
- Description: The Impala JDBC connection string. An example connection string using kerberos:
  jdbc:impala://<impalad-host>:21050;SSL=1;AuthMech=1;SSLTrustStorePwd=changeit;SSLTrustStore=/path/to/truststore;KrbHostFQDN=<impalad-host>;KrbServiceName=impala;KrbAuthType=0;KrbRealm=<realm>
- Required: false
- Default:

**archway.db.impala.driver**

- Label: Impala JDBC Driver
- Description: JDBC Driver to use with Impala
- Required:
- Default: com.cloudera.impala.jdbc41.Driver

**archway.ldap.provisioningBinding.bindDN**

- Label: Provisioning LDAP Admin DN
- Description: DN for a user that has privelege to create groups and modifying group membership in archway.ldap.baseDN
  There are two LDAP connections created, 'provisiong' and 'lookup'. Usually the same values can be used for both. In rare cases it's not possible to do a group lookup from the same connection where groups are created, in this case you can use different values for the 'lookup' connection'''
- Required: true
- Default:

**archway.ldap.provisioningBinding.server**

- Label: Provisioning LDAP Host
- Description: THe LDAP/AD host for the 'provisioning' LDAP connection
- Required: true
- Default:

**archway.ldap.provisioningBinding.port**

- Label: Provisioning LDAP Port
- Description: The LDAP/AD port the 'provisioning' LDAP connection
- Required: true
- Default: 389

**archway.ldap.provisioningBinding.bindPassword**

- Label: Provisioning LDAP Admin Password
- Description: The password for the admin user
- Required: true
- Default:

**archway.ldap.lookupBinding.bindDN**

- Label: Lookup LDAP Admin DN
- Description: The DN for the 'lookup' LDAP connection, used for authenticating users and validating group membership. This should usually be set to the same value as the corresponding provisioning value
- Required: true
- Default:

**archway.ldap.lookupBinding.server**

- Label: Lookup LDAP Host
- Description: THe LDAP/AD host for the 'lookup' LDAP connection. This should usually be set to the same value as the corresponding 'provisioning' value. If it is set to empty string 'provisioning' value is used instead
- Required: false
- Default:

**archway.ldap.lookupBinding.port**

- Label: Lookup LDAP Port
- Description: The LDAP/AD port for the 'lookup' LDAP connection. This should usually be set to the same value as the corresponding 'provisioning' value
- Required: false
- Default: 389

**archway.ldap.lookupBinding.bindPassword**

- Label: Lookup LDAP Admin Password
- Description: The password for the 'lookup' LDAP connection. This should usually be set to the same value as the corresponding 'provisioning' value
- Required: false
- Default:

**archway.ldap.baseDN**

- Label: Base DN
- Description: The base DN for user search when authenticating users to Archway
- Required: true
- Default:

**archway.ldap.domain**

- Label: AD Realm
- Description: The realm to use when looking up users (eg. EXAMPLE.COM)
- Required: true
- Default:

**archway.ldap.realm**

- Label: NIS Realm
- Description: The NIS domain for Linux attributes (eg. "example")
- Required: true
- Default:

**archway.ldap.groupPath**

- Label: Group DN
- Description: The LDAP location for groups. Groups needed for Sentry access control will be created in the DN
- Required: true
- Default:

**archway.ldap.syncInterval**

- Label: Synchronization interval
- Description: The time interval how often should AD groups be synchronized with database
- Required:
- Default: 2 hour

**archway.ldap.authorizationDN**

- Label: Archway Authorization Group DN
- Description: The LDAP DN used for authorizing users into Archway. If no value is provided users will be authenticated using Active Directory but no authorization will happen.
- Required: false
- Default:

**archway.approvers.notificationEmail**

- Label: Notification Email Address
- Description: The comma separated list of email addresses to send notification emails to
- Required: true
- Default:

**archway.approvers.infrastructure**

- Label: Operations Group DN
- Description: Members of this group have the ability to approve workspaces based on infrastructure availability and whether a project meets governance standards
- Required: true
- Default:

**archway.approvers.risk**

- Label: Risk Group DN
- Description: Members of this group will have the ability to approve workspaces based on whether they meet company risk compliance
- Required: true
- Default:

**archway.ui.url**

- Label: Archway UI
- Description: Full url for Archway UI in the format `https://host:port`. The default of this value should be auto generated and work without configuration
- Required: false
- Default:

**archway.ui.staticContentDir**

- Label: Archway UI static content directory
- Description: Path to Archway UI directory. It is an override and the original value comes from the env var `ARCHWAY_UI_HOME`
- Required: false
- Default:

**archway.smtp.fromEmail**

- Label: Notification Email Sender
- Description: The email address to send notification emails as
- Required: true
- Default:

**archway.smtp.host**

- Label: SMTP Host
- Description: SMTP Host
- Required: true
- Default:

**archway.smtp.port**

- Label: SMTP Port
- Description: SMTP Port
- Required: true
- Default:

**archway.smtp.ssl**

- Label: Enable SMTP starttls
- Description: Enable SMTP starttls
- Required: true
- Default:

**archway.smtp.smtps**

- Label: Enable SMTPS
- Description: Enable SMTPS (SSL)
- Required: true
- Default: False

**archway.smtp.auth**

- Label: SMTP Auth
- Description: SMTP Auth Required?
- Required: true
- Default:

**archway.smtp.user**

- Label: Username for SMTP
- Description: Username for SMTP
- Required: false
- Default:

**archway.smtp.pass**

- Label: Password for SMTP
- Description: Pssword for SMTP
- Required: false
- Default:

**archway.templates.ldapGroupGenerator**

- Label: Group Generator Class
- Description: Full class name reference for the LDAP generator to use.
- Required: false
- Default:

**archway.kafka.zookeeperConnect**

- Label: Kafka Zookeeper Connect String
- Description: The Kakfa zookeeper quorum connect string.
- Required: true
- Default:

**archway.java.truststore.location**

- Label: Java truststore location
- Description: Java truststore location
- Required: false
- Default:

**archway.java.truststore.password**

- Label: Java truststore password
- Description: Password for the Java truststore
- Required: false
- Default:

**archway.additional.java.options**

- Label: Java addition options
- Description: Injection of java options to the Archway Server process
- Required: true
- Default:

**archway.templates.templateRoot**

- Label: Workspace template root directory
- Description: Workspace template root directory
- Required: true
- Default:

**archway.kafka.secureTopics**

- Label: Secure Kafka Topics
- Description: Creates appropriate security groups and grants for Kafka topics
- Required: false
- Default: True

**archway.ldap.filterTemplate**

- Label: LDAP Filter Template
- Description: The Mustache template used for finding new members
- Required: false
- Default: (&(sAMAccountName={{ filter }}\*)(|(objectClass=user)(objectClass=group)))

**archway.ldap.memberDisplayTemplate**

- Label: Member Display Template
- Description: The Mustache template used for displaying members when adding and showing existing members
- Required: false
- Default: {{ sAMAccountName }} ({{ name }})

**archway.featureFlags**

- Label: Feature flags
- Description: List of feature flags which are enabled
- Required: false
- Default: []
