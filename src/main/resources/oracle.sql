CREATE TABLE heimdali.hive_database (
  id NUMBER GENERATED ALWAYS as IDENTITY(START with 1 INCREMENT by 1),
  name VARCHAR2(255) NOT NULL,
  role VARCHAR2(255) NOT NULL,
  location VARCHAR2(255) NOT NULL,
  size_in_gb NUMBER NOT NULL,
  PRIMARY KEY (id));

CREATE TABLE heimdali.yarn (
  id NUMBER GENERATED ALWAYS as IDENTITY(START with 1 INCREMENT by 1),
  pool_name VARCHAR2(255) NOT NULL,
  max_cores NUMBER NOT NULL,
  max_memory_in_gb NUMBER NOT NULL,
  PRIMARY KEY (id));

CREATE TABLE heimdali.ldap_registration (
  id NUMBER GENERATED ALWAYS as IDENTITY(START with 1 INCREMENT by 1),
  distinguished_name VARCHAR2(255) NOT NULL,
  common_name VARCHAR2(255) NOT NULL,
  PRIMARY KEY (id));

CREATE TABLE heimdali.dataset (
  id NUMBER GENERATED ALWAYS as IDENTITY(START with 1 INCREMENT by 1),
  name VARCHAR2(255) NOT NULL,
  system_name VARCHAR2(255) NOT NULL,
  purpose VARCHAR2(255) NOT NULL,
  yarn_id NUMBER NULL,
  hive_database_id NUMBER NULL,
  ldap_registration_id NUMBER NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_datasets_yarn1
    FOREIGN KEY (yarn_id)
    REFERENCES heimdali.yarn (id),
  CONSTRAINT fk_datasets_hive_database1
    FOREIGN KEY (hive_database_id)
    REFERENCES heimdali.hive_database (id),
  CONSTRAINT fk_datasets_ldap_registration1
    FOREIGN KEY (ldap_registration_id)
    REFERENCES heimdali.ldap_registration (id)
   );

CREATE INDEX fk_datasets_yarn1_idx ON heimdali.dataset (yarn_id ASC);
CREATE INDEX fk_datasets_hive_database1_idx ON heimdali.dataset (hive_database_id ASC);
CREATE INDEX fk_datasets_ldap_reg1_idx ON heimdali.dataset (ldap_registration_id ASC);

CREATE TABLE heimdali.compliance (
  id NUMBER GENERATED ALWAYS as IDENTITY(START with 1 INCREMENT by 1),
  phi_data NUMBER NOT NULL,
  pii_data NUMBER NOT NULL,
  pci_data NUMBER NOT NULL,
  PRIMARY KEY (id));

CREATE TABLE heimdali.governed_dataset (
  id NUMBER GENERATED ALWAYS as IDENTITY(START with 1 INCREMENT by 1),
  name VARCHAR2(255) NOT NULL,
  system_name VARCHAR2(255) NOT NULL,
  purpose VARCHAR2(255) NOT NULL,
  created TIMESTAMP NOT NULL,
  created_by VARCHAR2(255) NOT NULL,
  requested_size NUMBER NOT NULL,
  requested_cores NUMBER NOT NULL,
  requested_memory NUMBER NOT NULL,
  raw_dataset_id NUMBER NOT NULL,
  staging_dataset_id NUMBER NOT NULL,
  modeled_dataset_id NUMBER NOT NULL,
  compliance_id NUMBER NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_governed_raw_dataset
    FOREIGN KEY (raw_dataset_id)
    REFERENCES heimdali.dataset (id),
  CONSTRAINT fk_governed_staging_dataset
    FOREIGN KEY (staging_dataset_id)
    REFERENCES heimdali.dataset (id),
  CONSTRAINT fk_governed_modeled_dataset
    FOREIGN KEY (modeled_dataset_id)
    REFERENCES heimdali.dataset (id),
  CONSTRAINT fk_governed_compliance
    FOREIGN KEY (compliance_id)
    REFERENCES heimdali.compliance (id)
   );

CREATE INDEX fk_governed_raw_dataset_idx ON heimdali.governed_dataset (raw_dataset_id ASC);
CREATE INDEX fk_governed_staging_idx ON heimdali.governed_dataset (staging_dataset_id ASC);
CREATE INDEX fk_governed_modeled_idx ON heimdali.governed_dataset (modeled_dataset_id ASC);
CREATE INDEX fk_governed_compliance_idx ON heimdali.governed_dataset (compliance_id ASC);

CREATE TABLE heimdali.shared_workspace (
  id NUMBER GENERATED ALWAYS as IDENTITY(START with 1 INCREMENT by 1),
  name VARCHAR2(255) NOT NULL,
  system_name VARCHAR2(255) NOT NULL,
  purpose VARCHAR2(255) NOT NULL,
  created TIMESTAMP NOT NULL,
  created_by VARCHAR2(255) NOT NULL,
  requested_size NUMBER NOT NULL,
  requested_cores NUMBER NOT NULL,
  requested_memory NUMBER NOT NULL,
  yarn_id NUMBER NULL,
  hive_database_id NUMBER NULL,
  ldap_registration_id NUMBER NULL,
  compliance_id NUMBER NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_shared_workspaces_yarn1 FOREIGN KEY (yarn_id)
  REFERENCES heimdali.yarn(id),
  CONSTRAINT fk_sw_hive_database1
    FOREIGN KEY (hive_database_id)
    REFERENCES heimdali.hive_database (id),
  CONSTRAINT fk_sw_ldap_reg1
    FOREIGN KEY (ldap_registration_id)
    REFERENCES heimdali.ldap_registration (id),
  CONSTRAINT fk_sw_compliance1
    FOREIGN KEY (compliance_id)
    REFERENCES heimdali.compliance (id)
   );

CREATE INDEX fk_sw_yarn1_idx ON heimdali.shared_workspace (yarn_id ASC);
CREATE INDEX fk_sw_hive_database1_idx ON heimdali.shared_workspace (hive_database_id ASC);
CREATE INDEX fk_sw_ldap_registration1_idx ON heimdali.shared_workspace (ldap_registration_id ASC);
CREATE INDEX fk_sw_compliance1_idx ON heimdali.shared_workspace (compliance_id ASC);

CREATE TABLE heimdali.user_workspace (
  username VARCHAR2(255) NOT NULL,
  ldap_registration_id NUMBER NULL,
  hive_database_id NUMBER NULL,
  yarn_id NUMBER NULL,
  PRIMARY KEY (username),
  CONSTRAINT fk_users_ldap_registration
    FOREIGN KEY (ldap_registration_id)
    REFERENCES heimdali.ldap_registration (id),
  CONSTRAINT fk_users_hive_database1
    FOREIGN KEY (hive_database_id)
    REFERENCES heimdali.hive_database (id),
  CONSTRAINT fk_users_yarn1
    FOREIGN KEY (yarn_id)
    REFERENCES heimdali.yarn (id)
   );

CREATE INDEX fk_users_ldap_registration_idx ON heimdali.user_workspace (ldap_registration_id ASC);
CREATE INDEX fk_users_hive_database1_idx ON heimdali.user_workspace (hive_database_id ASC);
CREATE INDEX fk_users_yarn1_idx ON heimdali.user_workspace (yarn_id ASC);