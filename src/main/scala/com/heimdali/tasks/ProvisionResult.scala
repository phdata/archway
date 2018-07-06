package com.heimdali.tasks

sealed trait ProvisionResult { def message: String }

case class Error(message: String) extends ProvisionResult

case class Success(message: String) extends ProvisionResult