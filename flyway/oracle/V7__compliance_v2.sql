-- -----------------------------------------------------
-- Table `compliance_v2`
-- -----------------------------------------------------

CREATE TABLE compliance_v2 (
  id NUMBER GENERATED ALWAYS AS IDENTITY,
  question_id NUMBER NOT NULL,
  workspace_id NUMBER NOT NULL,
  group_id NUMBER NOT NULL,
  PRIMARY KEY (id));