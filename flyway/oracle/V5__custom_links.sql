-- -----------------------------------------------------
-- Table `custom_link_group`
-- -----------------------------------------------------

CREATE TABLE custom_link_group (
  id NUMBER GENERATED ALWAYS AS IDENTITY,
  name VARCHAR(255) NOT NULL,
  description VARCHAR(255) NOT NULL,
  PRIMARY KEY (id));

-- -----------------------------------------------------
-- Table `custom_link`
-- -----------------------------------------------------

CREATE TABLE custom_link (
  id NUMBER GENERATED ALWAYS AS IDENTITY,
  custom_link_group_id NUMBER NOT NULL,
  name VARCHAR(255) NOT NULL,
  description VARCHAR(255) NOT NULL,
  url VARCHAR(255) NOT NULL,
  PRIMARY KEY (id)
,
 CONSTRAINT fk_cust_ln_cust_ln_grp1
   FOREIGN KEY (custom_link_group_id)
   REFERENCES custom_link_group (id)
   )
;

CREATE INDEX fk_cust_ln_cust_ln_grp_idx ON custom_link (custom_link_group_id);