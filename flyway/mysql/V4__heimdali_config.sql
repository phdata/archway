CREATE TABLE IF NOT EXISTS heimdali_config (
  config_key VARCHAR(255) NOT NULL,
  config_value VARCHAR(255) NOT NULL,
  PRIMARY KEY (config_key));

INSERT INTO heimdali_config (config_key, config_value)
SELECT 'nextgid', coalesce(max(cast(key as bigint)), 1039493) + 1 from ldap_attribute where key = 'gidNumber';