CREATE TABLE IF NOT EXISTS `ldap_attribute` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `key` VARCHAR(255) NOT NULL,
  `value` VARCHAR(255) NOT NULL,
  `ldap_registration_id` BIGINT NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_ldap_attribute_ldap_registration1_idx` (`ldap_registration_id` ASC),
  CONSTRAINT `fk_ldap_attribute_ldap_registration1`
    FOREIGN KEY (`ldap_registration_id`)
    REFERENCES `heimdali`.`ldap_registration` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);

BEGIN;

INSERT INTO ldap_attribute (ldap_registration_id, `key`, value)
SELECT id, 'dn', distinguished_name FROM ldap_registration;

INSERT INTO ldap_attribute (ldap_registration_id, `key`, value)
SELECT id, 'objectClass', 'group' FROM ldap_registration;

INSERT INTO ldap_attribute (ldap_registration_id, `key`, value)
SELECT id, 'objectClass', 'top' FROM ldap_registration;

INSERT INTO ldap_attribute (ldap_registration_id, `key`, value)
SELECT id, 'sAMAccountName', common_name FROM ldap_registration;

INSERT INTO ldap_attribute (ldap_registration_id, `key`, value)
SELECT id, 'cn', common_name FROM ldap_registration;

INSERT INTO ldap_attribute (ldap_registration_id, `key`, value)
SELECT id, 'msSFU30Name', common_name FROM ldap_registration;

INSERT INTO ldap_attribute (ldap_registration_id, `key`, value)
SELECT id, 'msSFU30NisDomain', 'jotunn' FROM ldap_registration;

SET @gid_number = 1039493;

INSERT INTO ldap_attribute (ldap_registration_id, `key`, value)
SELECT id, 'gidNumber', (@gid_number:=@gid_number + 1) FROM ldap_registration;

COMMIT;
