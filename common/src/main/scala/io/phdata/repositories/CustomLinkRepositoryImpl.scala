package io.phdata.repositories
import cats.implicits._
import doobie._
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import io.phdata.models.CustomLink

class CustomLinkRepositoryImpl extends CustomLinkRepository {

  import CustomLinkRepositoryImpl.Statements._

  implicit val han = CustomLogHandler.logHandler(this.getClass)

  override def create(customLinkGroupId: Long, customLink: CustomLink): ConnectionIO[Long] =
    insertRecord(customLinkGroupId, customLink).withUniqueGeneratedKeys("id")

  override def update(customLink: CustomLink): ConnectionIO[Int] =
    updateRecord(customLink).run

  override def delete(customLinkId: Long): ConnectionIO[Unit] =
    deleteRecord(customLinkId).run.void

  override def findByCustomLinkGroupId(customLinkGroupId: Long): ConnectionIO[List[CustomLink]] =
    findRecordByCustomLinkGroupId(customLinkGroupId).to[List]
}

object CustomLinkRepositoryImpl {

  object Statements {

    def insertRecord(customLinkGroupId: Long, customLink: CustomLink): Update0 =
      sql"""
           insert into custom_link (custom_link_group_id, name, description, url)
           values (${customLinkGroupId}, ${customLink.name}, ${customLink.description}, ${customLink.url})
      """.update

    def updateRecord(customLink: CustomLink): Update0 =
      sql"""
          update custom_link set
          name = ${customLink.name},
          description = ${customLink.description},
          url = ${customLink.url}
      """.update

    def deleteRecord(customLinkId: Long): Update0 =
      sql"""delete from custom_link where id = ${customLinkId}""".update

    def findRecordByCustomLinkGroupId(customLinkGroupId: Long): Query0[CustomLink] =
      sql"""
         select
           id, name, description, url, custom_link_group_id
         from custom_link where custom_link_group_id = ${customLinkGroupId}
      """.query[CustomLink]
  }

}
