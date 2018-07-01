CREATE TABLE IF NOT EXISTS `heimdali`.`ldap_registration` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `distinguished_name` VARCHAR(255) NOT NULL,
  `common_name` VARCHAR(255) NOT NULL,
  `created` TIMESTAMP NULL,
  `existing` TINYINT NOT NULL DEFAULT 0,
  `sentry_role` VARCHAR(255) NOT NULL,
  PRIMARY KEY (`id`));

CREATE TABLE IF NOT EXISTS `heimdali`.`hive_database` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `location` VARCHAR(255) NOT NULL,
  `size_in_gb` INT NOT NULL,
  `created` TIMESTAMP NULL,
  `readonly_group_id` BIGINT NULL,
  `manager_group_id` BIGINT NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_hive_database_ldap_registration1_idx` (`readonly_group_id` ASC),
  INDEX `fk_hive_database_ldap_registration2_idx` (`manager_group_id` ASC),
  CONSTRAINT `fk_hive_database_ldap_registration1`
    FOREIGN KEY (`readonly_group_id`)
    REFERENCES `heimdali`.`ldap_registration` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_hive_database_ldap_registration2`
    FOREIGN KEY (`manager_group_id`)
    REFERENCES `heimdali`.`ldap_registration` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);

CREATE TABLE IF NOT EXISTS `heimdali`.`yarn` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `pool_name` VARCHAR(255) NOT NULL,
  `max_cores` INT NOT NULL,
  `max_memory_in_gb` INT NOT NULL,
  `created` TIMESTAMP NULL,
  `group_id` BIGINT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_yarn_ldap_registration1_idx` (`group_id` ASC),
  CONSTRAINT `fk_yarn_ldap_registration1`
    FOREIGN KEY (`group_id`)
    REFERENCES `heimdali`.`ldap_registration` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);

CREATE TABLE IF NOT EXISTS `heimdali`.`compliance` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `phi_data` TINYINT NOT NULL,
  `pii_data` TINYINT NOT NULL,
  `pci_data` TINYINT NOT NULL,
  PRIMARY KEY (`id`));

CREATE TABLE IF NOT EXISTS `heimdali`.`workspace_request` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `compliance_id` BIGINT NOT NULL,
  `requested_by` VARCHAR(255) NOT NULL,
  `request_date` TIMESTAMP NOT NULL,
  `single_user` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  INDEX `fk_workspace_request_compliance1_idx` (`compliance_id` ASC),
  CONSTRAINT `fk_workspace_request_compliance1`
    FOREIGN KEY (`compliance_id`)
    REFERENCES `heimdali`.`compliance` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `heimdali`.`approval` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `role` ENUM('infra', 'risk') NOT NULL,
  `approver` VARCHAR(255) NOT NULL,
  `approval_time` TIMESTAMP NOT NULL,
  `workspace_request_id` INT NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_approval_workspace_request1_idx` (`workspace_request_id` ASC),
  CONSTRAINT `fk_approval_workspace_request1`
    FOREIGN KEY (`workspace_request_id`)
    REFERENCES `heimdali`.`workspace_request` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `heimdali`.`request_hive` (
  `workspace_request_id` INT NOT NULL,
  `hive_database_id` BIGINT NOT NULL,
  PRIMARY KEY (`workspace_request_id`, `hive_database_id`),
  INDEX `fk_workspace_request_has_hive_database_hive_database1_idx` (`hive_database_id` ASC),
  INDEX `fk_workspace_request_has_hive_database_workspace_request1_idx` (`workspace_request_id` ASC),
  CONSTRAINT `fk_workspace_request_has_hive_database_workspace_request1`
    FOREIGN KEY (`workspace_request_id`)
    REFERENCES `heimdali`.`workspace_request` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_workspace_request_has_hive_database_hive_database1`
    FOREIGN KEY (`hive_database_id`)
    REFERENCES `heimdali`.`hive_database` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `heimdali`.`request_yarn` (
  `workspace_request_id` INT NOT NULL,
  `yarn_id` BIGINT NOT NULL,
  PRIMARY KEY (`workspace_request_id`, `yarn_id`),
  INDEX `fk_workspace_request_has_yarn_yarn1_idx` (`yarn_id` ASC),
  INDEX `fk_workspace_request_has_yarn_workspace_request1_idx` (`workspace_request_id` ASC),
  CONSTRAINT `fk_workspace_request_has_yarn_workspace_request1`
    FOREIGN KEY (`workspace_request_id`)
    REFERENCES `heimdali`.`workspace_request` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_workspace_request_has_yarn_yarn1`
    FOREIGN KEY (`yarn_id`)
    REFERENCES `heimdali`.`yarn` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `heimdali`.`member` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(255) NOT NULL,
  `ldap_registration_id` BIGINT NOT NULL,
  `created` TIMESTAMP NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_member_ldap_registration1_idx` (`ldap_registration_id` ASC),
  CONSTRAINT `fk_member_ldap_registration1`
    FOREIGN KEY (`ldap_registration_id`)
    REFERENCES `heimdali`.`ldap_registration` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;
