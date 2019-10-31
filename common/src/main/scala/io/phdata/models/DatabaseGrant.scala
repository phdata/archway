package io.phdata.models

case class DatabaseGrant(
    database: String,
    table: String,
    partition: String,
    column: String,
    principalName: String,
    principalType: String,
    privilege: String,
    grantOption: Boolean,
    grantTime: Long,
    grantor: String
)
