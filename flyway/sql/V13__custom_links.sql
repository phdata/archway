-- -----------------------------------------------------
-- Table `custom_link_group`
-- -----------------------------------------------------

CREATE SEQUENCE custom_link_group_seq;

CREATE TABLE IF NOT EXISTS custom_link_group (
  id BIGINT NOT NULL DEFAULT NEXTVAL ('custom_link_group_seq'),
  name VARCHAR(255) NOT NULL,
  description VARCHAR(255) NOT NULL,
  PRIMARY KEY (id));

-- -----------------------------------------------------
-- Table `custom_link`
-- -----------------------------------------------------

CREATE SEQUENCE custom_link_seq;

CREATE TABLE IF NOT EXISTS custom_link (
  id BIGINT NOT NULL DEFAULT NEXTVAL ('custom_link_seq'),
  custom_link_group_id BIGINT NOT NULL,
  name VARCHAR(255) NOT NULL,
  description VARCHAR(255) NOT NULL,
  url VARCHAR(255) NOT NULL,
  PRIMARY KEY (id)
,
  CONSTRAINT fk_custom_link_link_group1
  FOREIGN KEY (custom_link_group_id)
  REFERENCES custom_link_group (id)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION)
;

CREATE INDEX fk_custom_link_link_group1_idx ON custom_link (custom_link_group_id);