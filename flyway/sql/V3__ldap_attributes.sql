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

ALTER TABLE kafka_topic
    ALTER COLUMN partitions SET NOT NULL,
    ALTER COLUMN replication_factor SET NOT NULL;