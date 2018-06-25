package com.heimdali.models

import java.time.Instant

case class WorkspaceMember(username: String, created: Option[Instant])
