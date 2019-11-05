package io.phdata.repositories

import io.phdata.models.{ComplianceQuestion}
import doobie._
import doobie.implicits._

class ComplianceRepositoryImpl extends ComplianceRepository {

  implicit val han = CustomLogHandler.logHandler(this.getClass)

  def insertRecord(questionId: Long, workspaceId: Long, groupId: Long): ConnectionIO[Long] =
    sql"""
       insert into compliance (workspace_id, question_id, group_id)
       values(
        $workspaceId,
        $questionId,
        $groupId
       )
      """.update.withUniqueGeneratedKeys[Long]("id")

  override def create(compliance: List[ComplianceQuestion], workspaceId: Long): List[ConnectionIO[Long]] =
    compliance.map(question => insertRecord(question.id.get, workspaceId, question.complianceGroupId.get))

}
