package com.heimdali.repositories

import java.time.{Clock, Instant}

import cats.data.OptionT
import com.heimdali.models.ProvisionAttempt
import doobie._
import doobie.implicits._

class ProvisionAttemptRepositoryImpl(clock: Clock) extends ProvisionAttemptRepository {

  def insert(workspaceRequestId: Long): ConnectionIO[Long] =
    sql"insert into provision_attempt (workspace_request_id, started) values ($workspaceRequestId, ${Instant.now(clock)})"
      .update
      .withUniqueGeneratedKeys("id")

  def select(id: Long): OptionT[ConnectionIO, ProvisionAttempt] =
    OptionT(sql"select (id, started, finished) from provision_attempt where id = $id"
      .query[ProvisionAttempt]
      .option)

  override def create(workspaceRequestId: Long): ConnectionIO[ProvisionAttempt] =
    for {
      id <- insert(workspaceRequestId)
      result <- select(id).value
    } yield result.get

  override def addResult(id: Long, task: String, message: String): ConnectionIO[Int] =
    sql"insert into provision_result (provision_attempt_id, task_name, message, when) values ($id, $task, $message, ${Instant.now(clock)}"
    .update
    .run

}
