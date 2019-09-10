package io.phdata.repositories

import java.time.Instant

import cats.implicits._
import doobie._
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import io.phdata.models.{ComplianceGroup, ComplianceQuestion}

class ComplianceGroupRepositoryImpl extends ComplianceGroupRepository {

  import ComplianceGroupRepositoryImpl.Statements._

  implicit val han = CustomLogHandler.logHandler(this.getClass)

  override def create(complianceGroup: ComplianceGroup): ConnectionIO[Long] =
    insertRecord(complianceGroup).withUniqueGeneratedKeys[Long]("id")

  override def update(complianceGroupId: Long, complianceGroup: ComplianceGroup): ConnectionIO[Int] =
    updateRecord(complianceGroupId, complianceGroup).run

  override def list(): ConnectionIO[List[ComplianceGroup]] = {
    listRecords()
      .to[List]
      .map(
        _.groupBy(_._1)
          .mapValues(_.map(_._2))
          .map {
            case (k, v) =>
              ComplianceGroup(k.name, k.description, v, k.id)
          }
          .toList
      )
  }

  override def delete(complianceGroupId: Long): ConnectionIO[Unit] =
    deleteRecord(complianceGroupId).run.void
}

object ComplianceGroupRepositoryImpl {

  object Statements {

    type ComplianceGroupQuestion = (ComplianceGroup, ComplianceQuestion)

    def insertRecord(complianceGroup: ComplianceGroup): Update0 =
      sql"""
           insert into compliance_group (name, description)
           values(${complianceGroup.name}, ${complianceGroup.description})
         """.update

    def updateRecord(complianceGroupId: Long, complianceGroup: ComplianceGroup): Update0 =
      sql"""
           update compliance_group set
           name = ${complianceGroup.name},
           description = ${complianceGroup.description}
           where id = ${complianceGroupId}
         """.update

    def listRecords(): Query0[ComplianceGroupQuestion] =
      sql"""
           select
             g.id,
             g.name,
             g.description,

             q.id,
             q.question,
             q.requester,
             q.updated,
             q.compliance_group_id
           from
             compliance_group g
           inner join compliance_question q on q.compliance_group_id = g.id
           where g.deleted is NULL
      """.query[ComplianceGroupQuestion]

    def deleteRecord(complianceGroupId: Long): Update0 =
      sql"""update compliance_group set deleted = 1 where id = ${complianceGroupId}""".update
  }

}
