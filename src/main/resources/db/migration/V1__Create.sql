CREATE TABLE "hive_database" (
  "id" bigserial,
  "name" text,
  "role" text,
  "location" text,
  "size_in_gb" int,
  PRIMARY KEY ("id")
);

CREATE TABLE "datasets" (
  "id" bigserial,
  "name" text,
  "system_name" text,
  "purpose" text,
  "hive_database_id" bigint,
  "ldap_registration_id" bigint,
  "yarn_id" bigint,
  "compliance_id" bigint,
  PRIMARY KEY ("id")
);

CREATE TABLE "governed_datasets" (
  "id" bigserial,
  "name" text,
  "system_name" text,
  "purpose" text,
  "raw_dataset_id" bigint,
  "staging_dataset_id" bigint,
  "modeled_dataset_id" bigint,
  "created" timestamp,
  "created_by" text,
  "requested_size" int,
  "requested_cores" int,
  "requested_memory" int,
  PRIMARY KEY ("id")
);

CREATE TABLE "ldap_registration" (
  "id" bigserial,
  "dn" text,
  "cn" text,
  PRIMARY KEY ("id")
);

CREATE TABLE "shared_workspaces" (
  "id" bigserial,
  "name" text,
  "system_name" text,
  "purpose" text,
  "hive_database_id" bigint,
  "ldap_registration_id" bigint,
  "yarn_id" bigint,
  "compliance_id" bigint,
  "created" timestamp,
  "created_by" text,
  "requested_size" int,
  "requested_cores" int,
  "requested_memory" int,
  PRIMARY KEY ("id")
);

CREATE TABLE "yarn" (
  "id" bigserial,
  "pool_name" text,
  "max_cores" int,
  "max_memory_in_gb" int,
  PRIMARY KEY ("id")
);

CREATE TABLE "compliance" (
  "id" bigserial,
  "phi_data" boolean,
  "pii_data" boolean,
  "pci_data" boolean,
  PRIMARY KEY ("id")
);

CREATE TABLE "users" (
  "username" text,
  "hive_database_id" bigint,
  "ldap_registration_id" bigint,
  "yarn_id" bigint,
  PRIMARY KEY ("username")
);