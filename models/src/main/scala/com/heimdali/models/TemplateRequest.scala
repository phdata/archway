package com.heimdali.models

case class TemplateRequest(name: String, summary: String, description: String, compliance: Compliance, requester: String, templateName: String) {
  val generatedName: String =
    name
      .replaceAll("""[^a-zA-Z\d\s]""", " ") //replace with space to remove multiple underscores
      .trim //if we have leading or trailing spaces, clean them up
      .replaceAll("""\s+""", "_")
      .toLowerCase
}
