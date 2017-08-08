package com.heimdali.models

import org.joda.time.DateTime

case class Project(id: Option[Long], name: String, purpose: String, created: DateTime, createdBy: String)
