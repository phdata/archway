package io.phdata.repositories

import cats.implicits._
import doobie._
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import io.phdata.models.{CustomLink, CustomLinkGroup}

class CustomLinkGroupRepositoryImpl extends CustomLinkGroupRepository {

  import CustomLinkGroupRepositoryImpl.Statements._

  implicit val han = CustomLogHandler.logHandler(this.getClass)

  override def create(customLinkGroup: CustomLinkGroup): ConnectionIO[Long] =
    insertRecord(customLinkGroup).withUniqueGeneratedKeys("id")

  override def update(customLinkGroupId: Long, customLinkGroup: CustomLinkGroup): ConnectionIO[Int] =
    updateRecord(customLinkGroupId, customLinkGroup).run

  override def list: ConnectionIO[List[CustomLinkGroup]] =
    listRecords()
      .to[List]
      .map(
        _.groupBy(_._1)
          .mapValues(_.map(_._2))
          .map {
            case (k, v) =>
              CustomLinkGroup(k.name, k.description, v, k.id)
          }
          .toList
      )

  override def delete(customLinkGroupId: Long): ConnectionIO[Unit] =
    deleteRecord(customLinkGroupId).run.void
}

object CustomLinkGroupRepositoryImpl {

  object Statements {

    type CustomGroupLink = (CustomLinkGroup, CustomLink)

    def insertRecord(customLinkGroup: CustomLinkGroup): Update0 =
      sql"""
           insert into custom_link_group (name, description)
           values(${customLinkGroup.name}, ${customLinkGroup.description})
      """.update

    def updateRecord(customLinkGroupId: Long, customLinkGroup: CustomLinkGroup): Update0 =
      sql"""
           update custom_link_group set
           name = ${customLinkGroup.name},
           description = ${customLinkGroup.description}
           where id = ${customLinkGroupId}
         """.update

    def listRecords(): Query0[CustomGroupLink] =
      sql"""
           select
             g.id,
             g.name,
             g.description,

             l.id,
             l.name,
             l.description,
             l.url,
             l.custom_link_group_id
           from
             custom_link_group g
           inner join custom_link l on l.custom_link_group_id = g.id
      """.query[CustomGroupLink]

    def deleteRecord(customLinkGroupId: Long): Update0 =
      sql"""delete from custom_link_group where id = ${customLinkGroupId}""".update
  }
}
