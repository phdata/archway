-- -----------------------------------------------------
-- Table `compliance_v2`
-- -----------------------------------------------------

CREATE SEQUENCE compliance_v2_seq;

CREATE TABLE IF NOT EXISTS compliance_v2 (
  id BIGINT NOT NULL DEFAULT NEXTVAL ('compliance_v2_seq'),
  question_id BIGINT NOT NULL,
  workspace_id BIGINT NOT NULL,
  group_id BIGINT NOT NULL,
  PRIMARY KEY (id));
