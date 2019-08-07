
-- -----------------------------------------------------
-- Table `ldap_registration`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `ldap_registration` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `distinguished_name` VARCHAR(255) NOT NULL,
  `common_name` VARCHAR(255) NOT NULL,
  `existing` TINYINT NOT NULL DEFAULT 0,
  `sentry_role` VARCHAR(255) NOT NULL,
  `group_created` TIMESTAMP NULL,
  `role_created` TIMESTAMP NULL,
  `group_associated` TIMESTAMP NULL,
  PRIMARY KEY (`id`));


-- -----------------------------------------------------
-- Table `hive_grant`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `hive_grant` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `ldap_registration_id` BIGINT NOT NULL,
  `location_access` TIMESTAMP NULL,
  `database_access` TIMESTAMP NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_hive_grant_ldap_registration1_idx` (`ldap_registration_id` ASC),
  CONSTRAINT `fk_hive_grant_ldap_registration1`
    FOREIGN KEY (`ldap_registration_id`)
    REFERENCES `ldap_registration` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);


-- -----------------------------------------------------
-- Table `hive_database`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `hive_database` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `location` VARCHAR(255) NOT NULL,
  `size_in_gb` INT NOT NULL,
  `directory_created` TIMESTAMP NULL,
  `quota_set` TIMESTAMP NULL,
  `database_created` TIMESTAMP NULL,
  `manager_group_id` BIGINT NOT NULL,
  `readonly_group_id` BIGINT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_hive_database_hive_grant1_idx` (`manager_group_id` ASC),
  INDEX `fk_hive_database_hive_grant2_idx` (`readonly_group_id` ASC),
  CONSTRAINT `fk_hive_database_hive_grant1`
    FOREIGN KEY (`manager_group_id`)
    REFERENCES `hive_grant` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_hive_database_hive_grant2`
    FOREIGN KEY (`readonly_group_id`)
    REFERENCES `hive_grant` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);


-- -----------------------------------------------------
-- Table `resource_pool`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `resource_pool` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `pool_name` VARCHAR(255) NOT NULL,
  `max_cores` INT NOT NULL,
  `max_memory_in_gb` INT NOT NULL,
  `created` TIMESTAMP NULL,
  PRIMARY KEY (`id`));


-- -----------------------------------------------------
-- Table `compliance`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `compliance` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `phi_data` TINYINT NOT NULL,
  `pii_data` TINYINT NOT NULL,
  `pci_data` TINYINT NOT NULL,
  PRIMARY KEY (`id`));


-- -----------------------------------------------------
-- Table `workspace_request`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `workspace_request` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `compliance_id` BIGINT NOT NULL,
  `requested_by` VARCHAR(255) NOT NULL,
  `request_date` TIMESTAMP NOT NULL,
  `single_user` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  INDEX `fk_workspace_request_compliance1_idx` (`compliance_id` ASC),
  CONSTRAINT `fk_workspace_request_compliance1`
    FOREIGN KEY (`compliance_id`)
    REFERENCES `compliance` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);


-- -----------------------------------------------------
-- Table `approval`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `approval` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `role` ENUM('infra', 'risk') NOT NULL,
  `approver` VARCHAR(255) NOT NULL,
  `approval_time` TIMESTAMP NOT NULL,
  `workspace_request_id` BIGINT NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_approval_workspace_request1_idx` (`workspace_request_id` ASC),
  CONSTRAINT `fk_approval_workspace_request1`
    FOREIGN KEY (`workspace_request_id`)
    REFERENCES `workspace_request` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);


-- -----------------------------------------------------
-- Table `member`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `member` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `distinguished_name` VARCHAR(255) NOT NULL,
  `ldap_registration_id` BIGINT NOT NULL,
  `created` TIMESTAMP NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_member_ldap_registration1_idx` (`ldap_registration_id` ASC),
  CONSTRAINT `fk_member_ldap_registration1`
    FOREIGN KEY (`ldap_registration_id`)
    REFERENCES `ldap_registration` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);


-- -----------------------------------------------------
-- Table `provision_task`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `provision_task` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `completed` TIMESTAMP NULL,
  PRIMARY KEY (`id`));


-- -----------------------------------------------------
-- Table `task_log`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `task_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `message` VARCHAR(255) NOT NULL,
  `provision_task_id` BIGINT NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_table1_provision_task1_idx` (`provision_task_id` ASC),
  CONSTRAINT `fk_table1_provision_task1`
    FOREIGN KEY (`provision_task_id`)
    REFERENCES `provision_task` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);


-- -----------------------------------------------------
-- Table `topic_grant`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `topic_grant` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `ldap_registration_id` BIGINT NOT NULL,
  `topic_access` TIMESTAMP NULL,
  `actions` VARCHAR(255) NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_topic_role_ldap_registration1_idx` (`ldap_registration_id` ASC),
  CONSTRAINT `fk_topic_role_ldap_registration1`
    FOREIGN KEY (`ldap_registration_id`)
    REFERENCES `ldap_registration` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);


-- -----------------------------------------------------
-- Table `kafka_topic`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `kafka_topic` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `partitions` INT NULL,
  `replication_factor` INT NULL,
  `manager_role_id` BIGINT NOT NULL,
  `readonly_role_id` BIGINT NOT NULL,
  `topic_created` TIMESTAMP NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_kafka_topic_topic_role1_idx` (`manager_role_id` ASC),
  INDEX `fk_kafka_topic_topic_role2_idx` (`readonly_role_id` ASC),
  CONSTRAINT `fk_kafka_topic_topic_role1`
    FOREIGN KEY (`manager_role_id`)
    REFERENCES `topic_grant` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_kafka_topic_topic_role2`
    FOREIGN KEY (`readonly_role_id`)
    REFERENCES `topic_grant` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);


-- -----------------------------------------------------
-- Table `application`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `application` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(45) NOT NULL,
  `consumer_group_name` VARCHAR(255) NOT NULL,
  `ldap_registration_id` BIGINT NOT NULL,
  `consumer_group_access` TIMESTAMP NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_application_ldap_registration1_idx` (`ldap_registration_id` ASC),
  CONSTRAINT `fk_application_ldap_registration1`
    FOREIGN KEY (`ldap_registration_id`)
    REFERENCES `ldap_registration` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);


-- -----------------------------------------------------
-- Table `workspace_topic`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `workspace_topic` (
  `workspace_request_id` BIGINT NOT NULL,
  `kafka_topic_id` BIGINT NOT NULL,
  PRIMARY KEY (`workspace_request_id`, `kafka_topic_id`),
  INDEX `fk_workspace_request_has_kafka_topic_kafka_topic1_idx` (`kafka_topic_id` ASC),
  INDEX `fk_workspace_request_has_kafka_topic_workspace_request1_idx` (`workspace_request_id` ASC),
  CONSTRAINT `fk_workspace_request_has_kafka_topic_workspace_request1`
    FOREIGN KEY (`workspace_request_id`)
    REFERENCES `workspace_request` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_workspace_request_has_kafka_topic_kafka_topic1`
    FOREIGN KEY (`kafka_topic_id`)
    REFERENCES `kafka_topic` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);


-- -----------------------------------------------------
-- Table `workspace_database`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `workspace_database` (
  `workspace_request_id` BIGINT NOT NULL,
  `hive_database_id` BIGINT NOT NULL,
  PRIMARY KEY (`workspace_request_id`, `hive_database_id`),
  INDEX `fk_workspace_request_has_hive_database_hive_database1_idx` (`hive_database_id` ASC),
  INDEX `fk_workspace_request_has_hive_database_workspace_request1_idx` (`workspace_request_id` ASC),
  CONSTRAINT `fk_workspace_request_has_hive_database_workspace_request1`
    FOREIGN KEY (`workspace_request_id`)
    REFERENCES `workspace_request` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_workspace_request_has_hive_database_hive_database1`
    FOREIGN KEY (`hive_database_id`)
    REFERENCES `hive_database` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);


-- -----------------------------------------------------
-- Table `workspace_pool`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `workspace_pool` (
  `workspace_request_id` BIGINT NOT NULL,
  `resource_pool_id` BIGINT NOT NULL,
  PRIMARY KEY (`workspace_request_id`, `resource_pool_id`),
  INDEX `fk_workspace_request_has_resource_pool_resource_pool1_idx` (`resource_pool_id` ASC),
  INDEX `fk_workspace_request_has_resource_pool_workspace_request1_idx` (`workspace_request_id` ASC),
  CONSTRAINT `fk_workspace_request_has_resource_pool_workspace_request1`
    FOREIGN KEY (`workspace_request_id`)
    REFERENCES `workspace_request` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_workspace_request_has_resource_pool_resource_pool1`
    FOREIGN KEY (`resource_pool_id`)
    REFERENCES `resource_pool` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);


-- -----------------------------------------------------
-- Table `workspace_application`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `workspace_application` (
  `application_id` BIGINT NOT NULL,
  `workspace_request_id` BIGINT NOT NULL,
  PRIMARY KEY (`application_id`, `workspace_request_id`),
  INDEX `fk_application_has_workspace_request_workspace_request1_idx` (`workspace_request_id` ASC),
  INDEX `fk_application_has_workspace_request_application1_idx` (`application_id` ASC),
  CONSTRAINT `fk_application_has_workspace_request_application1`
    FOREIGN KEY (`application_id`)
    REFERENCES `application` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_application_has_workspace_request_workspace_request1`
    FOREIGN KEY (`workspace_request_id`)
    REFERENCES `workspace_request` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);
