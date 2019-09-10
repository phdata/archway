-- -----------------------------------------------------
-- Table `compliance_group`
-- -----------------------------------------------------

CREATE TABLE IF NOT EXISTS `compliance_group` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL UNIQUE,
  `description` VARCHAR(255) NOT NULL,
  `deleted` CHAR(1) NULL,
  PRIMARY KEY (`id`));

-- -----------------------------------------------------
-- Table `compliance_question`
-- -----------------------------------------------------

CREATE TABLE IF NOT EXISTS `compliance_question` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `compliance_group_id` BIGINT NOT NULL,
  `question` VARCHAR(255) NOT NULL,
  `requester` VARCHAR(255) NOT NULL,
  `updated` TIMESTAMP NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_compliance_question_compliance_group1_idx` (`compliance_group_id` ASC),
  CONSTRAINT `fk_compliance_question_compliance_group1`
    FOREIGN KEY (`compliance_group_id`)
    REFERENCES `compliance_group` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);