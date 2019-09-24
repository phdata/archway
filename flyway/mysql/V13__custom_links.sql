-- -----------------------------------------------------
-- Table `custom_link_group`
-- -----------------------------------------------------

CREATE TABLE IF NOT EXISTS `custom_link_group` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `description` VARCHAR(255) NOT NULL,
  PRIMARY KEY (`id`));

-- -----------------------------------------------------
-- Table `custom_link`
-- -----------------------------------------------------

CREATE TABLE IF NOT EXISTS `custom_link` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `custom_link_group_id` BIGINT NOT NULL,
  `name` VARCHAR(255) NOT NULL,
  `description` VARCHAR(255) NOT NULL,
  `url` VARCHAR(255) NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_custom_link_link_group1_idx` (`custom_link_group_id` ASC),
  CONSTRAINT `fk_custom_link_link_group1`
    FOREIGN KEY (`custom_link_group_id`)
    REFERENCES `custom_link_group` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);