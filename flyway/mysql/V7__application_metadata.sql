ALTER TABLE `application`
    ADD COLUMN `application_type` VARCHAR(255) NULL,
    ADD COLUMN `logo` VARCHAR(255) NULL,
    ADD COLUMN `language` VARCHAR(255) NULL,
    ADD COLUMN `repository` VARCHAR(255) NULL;