CREATE TABLE IF NOT EXISTS heimdali_config (
  config_key VARCHAR(255) NOT NULL,
  config_value VARCHAR(255) NOT NULL,
  PRIMARY KEY (config_key));

INSERT INTO heimdali_config (config_key, config_value)
VALUES ('nextgid', '1039494');