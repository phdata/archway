package com.heimdali.models

import java.time.LocalDateTime

case class Project(id: Long,
                   name: String,
                   purpose: String,
                   ldapDn: Option[String],
                   systemName: String,
                   created: LocalDateTime,
                   createdBy: String) {
  val generatedName =
    name
      .replaceAll("""[^a-zA-Z\d\s]""", " ") //replace with space to remove multiple underscores
      .trim //if we have leading or trailing spaces, clean them up
      .replaceAll("""\s+""", "_")
      .toLowerCase
}