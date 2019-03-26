ALTER TABLE kafka_topic
    ALTER COLUMN partitions SET NOT NULL,
    ALTER COLUMN replication_factor SET NOT NULL;