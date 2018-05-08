
-- -----------------------------------------------------
-- Schema heimdali
-- -----------------------------------------------------

-- -----------------------------------------------------
-- Schema heimdali
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `heimdali` DEFAULT CHARACTER SET utf8 ;
USE `heimdali` ;

-- -----------------------------------------------------
-- Table `heimdali`.`hive_database`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `heimdali`.`hive_database` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `role` VARCHAR(255) NOT NULL,
  `location` VARCHAR(255) NOT NULL,
  `size_in_gb` INT NOT NULL,
  PRIMARY KEY (`id`));


-- -----------------------------------------------------
-- Table `heimdali`.`yarn`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `heimdali`.`yarn` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `pool_name` VARCHAR(255) NOT NULL,
  `max_cores` INT NOT NULL,
  `max_memory_in_gb` INT NOT NULL,
  PRIMARY KEY (`id`));


-- -----------------------------------------------------
-- Table `heimdali`.`ldap_registration`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `heimdali`.`ldap_registration` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `distinguished_name` VARCHAR(255) NOT NULL,
  `common_name` VARCHAR(255) NOT NULL,
  PRIMARY KEY (`id`));


-- -----------------------------------------------------
-- Table `heimdali`.`dataset`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `heimdali`.`dataset` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `system_name` VARCHAR(255) NOT NULL,
  `purpose` VARCHAR(255) NOT NULL,
  `yarn_id` BIGINT NULL,
  `hive_database_id` BIGINT NULL,
  `ldap_registration_id` BIGINT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_datasets_yarn1_idx` (`yarn_id` ASC),
  INDEX `fk_datasets_hive_database1_idx` (`hive_database_id` ASC),
  INDEX `fk_datasets_ldap_registration1_idx` (`ldap_registration_id` ASC),
  CONSTRAINT `fk_datasets_yarn1`
    FOREIGN KEY (`yarn_id`)
    REFERENCES `heimdali`.`yarn` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_datasets_hive_database1`
    FOREIGN KEY (`hive_database_id`)
    REFERENCES `heimdali`.`hive_database` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_datasets_ldap_registration1`
    FOREIGN KEY (`ldap_registration_id`)
    REFERENCES `heimdali`.`ldap_registration` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);


-- -----------------------------------------------------
-- Table `heimdali`.`compliance`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `heimdali`.`compliance` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `phi_data` TINYINT NOT NULL,
  `pii_data` TINYINT NOT NULL,
  `pci_data` TINYINT NOT NULL,
  PRIMARY KEY (`id`));


-- -----------------------------------------------------
-- Table `heimdali`.`governed_dataset`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `heimdali`.`governed_dataset` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `system_name` VARCHAR(255) NOT NULL,
  `purpose` VARCHAR(255) NOT NULL,
  `created` TIMESTAMP NOT NULL,
  `created_by` VARCHAR(255) NOT NULL,
  `requested_size` INT NOT NULL,
  `requested_cores` INT NOT NULL,
  `requested_memory` INT NOT NULL,
  `raw_dataset_id` BIGINT NOT NULL,
  `staging_dataset_id` BIGINT NOT NULL,
  `modeled_dataset_id` BIGINT NOT NULL,
  `compliance_id` BIGINT NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_governed_datasets_raw_dataset_idx` (`raw_dataset_id` ASC),
  INDEX `fk_governed_datasets_staging_dataset_idx` (`staging_dataset_id` ASC),
  INDEX `fk_governed_datasets_modeled_dataset_idx` (`modeled_dataset_id` ASC),
  INDEX `fk_governed_datasets_compliance_idx` (`compliance_id` ASC),
  CONSTRAINT `fk_governed_datasets_raw_dataset`
    FOREIGN KEY (`raw_dataset_id`)
    REFERENCES `heimdali`.`dataset` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_governed_datasets_staging_dataset`
    FOREIGN KEY (`staging_dataset_id`)
    REFERENCES `heimdali`.`dataset` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_governed_datasets_modeled_dataset`
    FOREIGN KEY (`modeled_dataset_id`)
    REFERENCES `heimdali`.`dataset` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_governed_datasets_compliance`
    FOREIGN KEY (`compliance_id`)
    REFERENCES `heimdali`.`compliance` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);


-- -----------------------------------------------------
-- Table `heimdali`.`shared_workspace`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `heimdali`.`shared_workspace` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `system_name` VARCHAR(255) NOT NULL,
  `purpose` VARCHAR(255) NOT NULL,
  `created` TIMESTAMP NOT NULL,
  `created_by` VARCHAR(255) NOT NULL,
  `requested_size` INT NOT NULL,
  `requested_cores` INT NOT NULL,
  `requested_memory` INT NOT NULL,
  `yarn_id` BIGINT NULL,
  `hive_database_id` BIGINT NULL,
  `ldap_registration_id` BIGINT NULL,
  `compliance_id` BIGINT NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_shared_workspaces_yarn1_idx` (`yarn_id` ASC),
  INDEX `fk_shared_workspaces_hive_database1_idx` (`hive_database_id` ASC),
  INDEX `fk_shared_workspaces_ldap_registration1_idx` (`ldap_registration_id` ASC),
  INDEX `fk_shared_workspaces_compliance1_idx` (`compliance_id` ASC),
  CONSTRAINT `fk_shared_workspaces_yarn1`
    FOREIGN KEY (`yarn_id`)
    REFERENCES `heimdali`.`yarn` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_shared_workspaces_hive_database1`
    FOREIGN KEY (`hive_database_id`)
    REFERENCES `heimdali`.`hive_database` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_shared_workspaces_ldap_registration1`
    FOREIGN KEY (`ldap_registration_id`)
    REFERENCES `heimdali`.`ldap_registration` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_shared_workspaces_compliance1`
    FOREIGN KEY (`compliance_id`)
    REFERENCES `heimdali`.`compliance` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);


-- -----------------------------------------------------
-- Table `heimdali`.`user_workspace`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `heimdali`.`user_workspace` (
  `username` VARCHAR(255) NOT NULL,
  `ldap_registration_id` BIGINT NULL,
  `hive_database_id` BIGINT NULL,
  `yarn_id` BIGINT NULL,
  PRIMARY KEY (`username`),
  INDEX `fk_users_ldap_registration_idx` (`ldap_registration_id` ASC),
  INDEX `fk_users_hive_database1_idx` (`hive_database_id` ASC),
  INDEX `fk_users_yarn1_idx` (`yarn_id` ASC),
  CONSTRAINT `fk_users_ldap_registration`
    FOREIGN KEY (`ldap_registration_id`)
    REFERENCES `heimdali`.`ldap_registration` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_users_hive_database1`
    FOREIGN KEY (`hive_database_id`)
    REFERENCES `heimdali`.`hive_database` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_users_yarn1`
    FOREIGN KEY (`yarn_id`)
    REFERENCES `heimdali`.`yarn` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);