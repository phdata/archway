-- -----------------------------------------------------
-- Table `compliance_group`
-- -----------------------------------------------------

CREATE TABLE compliance_group (
  id NUMBER GENERATED ALWAYS AS IDENTITY,
  name VARCHAR(255) NOT NULL UNIQUE,
  description VARCHAR(255) NOT NULL,
  deleted CHAR(1) NULL,
  PRIMARY KEY (id));

-- -----------------------------------------------------
-- Table `compliance_question`
-- -----------------------------------------------------

CREATE TABLE compliance_question (
  id NUMBER GENERATED ALWAYS AS IDENTITY,
  compliance_group_id NUMBER NOT NULL,
  question VARCHAR(255) NOT NULL,
  requester VARCHAR(255) NOT NULL,
  updated TIMESTAMP(0) NOT NULL,
  PRIMARY KEY (id)
  ,
  CONSTRAINT fk_compl_q_compl_grp1
    FOREIGN KEY (compliance_group_id)
    REFERENCES compliance_group (id)
    )
;

CREATE INDEX fk_compl_q_compl_grp_idx ON compliance_question (compliance_group_id);