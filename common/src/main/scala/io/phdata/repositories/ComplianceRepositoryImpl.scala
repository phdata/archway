package io.phdata.repositories

import io.phdata.models.{ComplianceQuestion}
import doobie._
import doobie.implicits._

class ComplianceRepositoryImpl extends ComplianceRepository {

  implicit val han = CustomLogHandler.logHandler(this.getClass)

  override def create(compliance: List[ComplianceQuestion], workspaceId: Long): List[ConnectionIO[Long]] =
    compliance.map(question => Statements.insert(question.id.get, workspaceId, question.complianceGroupId.get).withUniqueGeneratedKeys[Long]("id"))

  override def findByWorkspaceId(workspaceId: Long): ConnectionIO[List[ComplianceQuestion]] =
    Statements.findByWorkspaceId(workspaceId).to[List]

  object Statements {

    def insert(questionId: Long, workspaceId: Long, groupId: Long): Update0 =
      sql"""
       insert into compliance (workspace_id, question_id, group_id)
       values(
        $workspaceId,
        $questionId,
        $groupId
       )
      """.update

    def findByWorkspaceId(workspaceId: Long): Query0[ComplianceQuestion] =
      sql"""
           select
              cq.*
           from compliance_v2 c.workspace_id = $workspaceId
           inner join compliance_questions cq on cq.id = c.question_id
          """.query[ComplianceQuestion]
  }
}