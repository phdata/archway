CREATE SEQUENCE ldap_attribute_seq;

CREATE TABLE IF NOT EXISTS ldap_attribute (
  id BIGINT NOT NULL DEFAULT NEXTVAL ('ldap_attribute_seq'),
  key VARCHAR(255) NOT NULL,
  value VARCHAR(255) NOT NULL,
  ldap_registration_id BIGINT NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_ldap_attribute_ldap_registration1
    FOREIGN KEY (ldap_registration_id)
    REFERENCES ldap_registration (id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);

CREATE INDEX fk_ldap_attribute_ldap_attribute_idx ON ldap_attribute(ldap_registration_id);

BEGIN;

INSERT INTO ldap_attribute (ldap_registration_id, key, value)
SELECT id, 'dn', distinguished_name FROM ldap_registration;

INSERT INTO ldap_attribute (ldap_registration_id, key, value)
SELECT id, 'objectClass', 'group' FROM ldap_registration;

INSERT INTO ldap_attribute (ldap_registration_id, key, value)
SELECT id, 'objectClass', 'top' FROM ldap_registration;

INSERT INTO ldap_attribute (ldap_registration_id, key, value)
SELECT id, 'sAMAccountName', common_name FROM ldap_registration;

INSERT INTO ldap_attribute (ldap_registration_id, key, value)
SELECT id, 'cn', common_name FROM ldap_registration;

INSERT INTO ldap_attribute (ldap_registration_id, key, value)
SELECT id, 'msSFU30Name', common_name FROM ldap_registration;

INSERT INTO ldap_attribute (ldap_registration_id, key, value)
SELECT id, 'msSFU30NisDomain', 'jotunn' FROM ldap_registration;

INSERT INTO ldap_attribute (ldap_registration_id, key, value)
SELECT id, 'gidNumber', ROW_NUMBER() OVER (ORDER BY id) + 1039493 FROM ldap_registration;

COMMIT;