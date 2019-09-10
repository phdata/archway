package io.phdata.repositories
import cats.implicits._
import doobie._
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import io.phdata.models.ComplianceQuestion

class ComplianceQuestionRepositoryImpl extends ComplianceQuestionRepository {

  import ComplianceQuestionRepositoryImpl.Statements._

  implicit val han = CustomLogHandler.logHandler(this.getClass)

  override def create(complianceGroupId: Long, complianceQuestion: ComplianceQuestion): ConnectionIO[Long] =
    insertRecord(complianceGroupId, complianceQuestion).withUniqueGeneratedKeys("id")

  override def update(complianceQuestion: ComplianceQuestion): ConnectionIO[Int] =
    updateRecord(complianceQuestion).run

  override def delete(complianceQuestionId: Long): ConnectionIO[Unit] = deleteRecord(complianceQuestionId).run.void

  override def findByComplianceGroupId(complianceGroupId: Long): ConnectionIO[List[ComplianceQuestion]] =
    findRecordByComplianceGroupId(complianceGroupId).to[List]
}

object ComplianceQuestionRepositoryImpl {

  object Statements {

    def insertRecord(complianceGroupId: Long, complianceQuestion: ComplianceQuestion): Update0 =
      sql"""
           insert into compliance_question (compliance_group_id, question, requester, updated)
           values (${complianceGroupId}, ${complianceQuestion.question}, ${complianceQuestion.requester}, ${complianceQuestion.updated})
         """.update

    def updateRecord(complianceQuestion: ComplianceQuestion): Update0 =
      sql"""
          update compliance_question set
          question = ${complianceQuestion.question},
          requester = ${complianceQuestion.requester},
          updated = ${complianceQuestion.updated}
      """.update

    def deleteRecord(complianceQuestionId: Long): Update0 =
      sql"""delete from compliance_question where id = ${complianceQuestionId}""".update

    def findRecordByComplianceGroupId(complianceGroupId: Long): Query0[ComplianceQuestion] =
      sql"""
         select
           id, question, requester, updated, compliance_group_id
         from compliance_question where compliance_group_id = ${complianceGroupId}
      """.query[ComplianceQuestion]
  }
}
