-- -----------------------------------------------------
-- Table `compliance_v2`
-- -----------------------------------------------------

CREATE TABLE IF NOT EXISTS `compliance_v2` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `question_id` BIGINT NOT NULL,
  `workspace_id` BIGINT NOT NULL,
  `group_id` BIGINT NOT NULL,
  PRIMARY KEY (`id`));