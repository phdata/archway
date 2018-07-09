package com.heimdali.repositories

import com.heimdali.models.ProvisionAttempt
import doobie._

trait ProvisionAttemptRepository {

  def create(workspaceRequestId: Long): ConnectionIO[ProvisionAttempt]

  def addResult(id: Long, task: String, message: String): ConnectionIO[Int]

}
