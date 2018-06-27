CREATE TABLE heimdali.ldap_registration (
  id NUMBER(19) NOT NULL GENERATED ALWAYS AS IDENTITY,
  distinguished_name VARCHAR(255) NOT NULL,
  common_name VARCHAR(255) NOT NULL,
  created DATE NULL,
  existing NUMBER(3) NOT NULL DEFAULT 0,
  sentry_role VARCHAR(255) NOT NULL,
  PRIMARY KEY (id));


CREATE TABLE IF NOT EXISTS heimdali.hive_database (
  id NUMBER(19) NOT NULL GENERATED ALWAYS AS IDENTITY,
  name VARCHAR(255) NOT NULL,
  location VARCHAR(255) NOT NULL,
  size_in_gb NUMBER(19) NOT NULL,
  created DATE NULL,
  readonly_group_id NUMBER(19) NULL,
  manager_group_id NUMBER(19) NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_hive_database_ldap_registration1
    FOREIGN KEY (readonly_group_id)
    REFERENCES heimdali.ldap_registration (id),
  CONSTRAINT fk_hive_database_ldap_registration2
    FOREIGN KEY (manager_group_id)
    REFERENCES heimdali.ldap_registration (id));


CREATE TABLE IF NOT EXISTS heimdali.yarn (
  id NUMBER(19) NOT NULL GENERATED ALWAYS AS IDENTITY,
  pool_name VARCHAR(255) NOT NULL,
  max_cores NUMBER(19) NOT NULL,
  max_memory_in_gb NUMBER(19) NOT NULL,
  created DATE NULL,
  group_id NUMBER(19) NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_yarn_ldap_registration1
    FOREIGN KEY (group_id)
    REFERENCES heimdali.ldap_registration (id));


CREATE TABLE IF NOT EXISTS heimdali.compliance (
  id NUMBER(19) NOT NULL GENERATED ALWAYS AS IDENTITY,
  phi_data NUMBER(3) NOT NULL,
  pii_data NUMBER(3) NOT NULL,
  pci_data NUMBER(3) NOT NULL,
  PRIMARY KEY (id));


CREATE TABLE IF NOT EXISTS heimdali.workspace_request (
  id NUMBER(19) NOT NULL GENERATED ALWAYS AS IDENTITY,
  name VARCHAR(255) NOT NULL,
  compliance_id NUMBER(19) NOT NULL,
  requested_by VARCHAR(255) NOT NULL,
  request_date DATE NOT NULL,
  single_user NUMBER(3) NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  CONSTRAINT fk_workspace_request_compliance1
    FOREIGN KEY (compliance_id)
    REFERENCES heimdali.compliance (id))


CREATE TABLE IF NOT EXISTS heimdali.approval (
  id NUMBER(19) NOT NULL GENERATED ALWAYS AS IDENTITY,
  role ENUM('infra', 'risk') NOT NULL,
  approver VARCHAR(255) NOT NULL,
  approval_time DATE NOT NULL,
  workspace_request_id NUMBER(19) NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_approval_workspace_request1
    FOREIGN KEY (workspace_request_id)
    REFERENCES heimdali.workspace_request (id))


CREATE TABLE IF NOT EXISTS heimdali.request_hive (
  workspace_request_id NUMBER(19) NOT NULL,
  hive_database_id NUMBER(19) NOT NULL,
  PRIMARY KEY (workspace_request_id, hive_database_id),
  CONSTRAINT fk_workspace_request_has_hive_database_workspace_request1
    FOREIGN KEY (workspace_request_id)
    REFERENCES heimdali.workspace_request (id),
  CONSTRAINT fk_workspace_request_has_hive_database_hive_database1
    FOREIGN KEY (hive_database_id)
    REFERENCES heimdali.hive_database (id))


CREATE TABLE IF NOT EXISTS heimdali.request_yarn (
  workspace_request_id NUMBER(19) NOT NULL,
  yarn_id NUMBER(19) NOT NULL,
  PRIMARY KEY (workspace_request_id, yarn_id),
  CONSTRAINT fk_workspace_request_has_yarn_workspace_request1
    FOREIGN KEY (workspace_request_id)
    REFERENCES heimdali.workspace_request (id),
  CONSTRAINT fk_workspace_request_has_yarn_yarn1
    FOREIGN KEY (yarn_id)
    REFERENCES heimdali.yarn (id))


CREATE TABLE IF NOT EXISTS heimdali.member (
  id NUMBER(19) NOT NULL GENERATED ALWAYS AS IDENTITY,
  username VARCHAR(255) NOT NULL,
  ldap_registration_id NUMBER(19) NOT NULL,
  created DATE NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_member_ldap_registration1
    FOREIGN KEY (ldap_registration_id)
    REFERENCES heimdali.ldap_registration (id))
