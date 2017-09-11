package com.heimdali.models

import java.time.LocalDateTime

case class Project(id: Long, name: String, purpose: String, created: LocalDateTime, createdBy: String)
