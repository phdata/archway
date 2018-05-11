package com.heimdali.repositories

import com.heimdali.models._
import scalikejdbc._

import scala.concurrent.{ExecutionContext, Future}

class SharedWorkspaceRepositoryImpl(implicit ec: ExecutionContext)
  extends SharedWorkspaceRepository {

  def baseSql(insidePart: (QuerySQLSyntaxProvider[_, _], QuerySQLSyntaxProvider[_, _], QuerySQLSyntaxProvider[_, _], QuerySQLSyntaxProvider[_, _], QuerySQLSyntaxProvider[_, _]) => (ConditionSQLBuilder[_] => ConditionSQLBuilder[_]))(implicit session: DBSession): SQL[SharedWorkspace, HasExtractor] = {
    val sw = SharedWorkspace.syntax
    val l = LDAPRegistration.syntax
    val c = Compliance.syntax
    val h = HiveDatabase.syntax
    val y = Yarn.syntax
    withSQL {
      select
        .from(SharedWorkspace as sw)
        .leftJoin(LDAPRegistration as l).on(sw.ldapRegistrationId, l.id)
        .leftJoin(Compliance as c).on(sw.complianceId, c.id)
        .leftJoin(HiveDatabase as h).on(sw.hiveDatabaseId, h.id)
        .leftJoin(Yarn as y).on(sw.yarnId, y.id)
        .where.withRoundBracket(insidePart(sw, l, c, h, y))
    }
      .map(SharedWorkspace(sw.resultName, l.resultName, c.resultName, h.resultName, y.resultName))
  }


  override def find(id: Long): Future[Option[SharedWorkspace]] = Future {
    NamedDB('default) readOnly { implicit session =>
      baseSql((sw, _, _, _, _) => _.eq(sw.id, id))
        .single
        .apply()
    }
  }

  def list(names: Seq[String]): Future[Seq[SharedWorkspace]] = Future {
    NamedDB('default) readOnly { implicit session =>
      baseSql((sw, _, _, _, _) => _.in(sw.systemName, names))
        .list
        .apply()
    }
  }

  def create(sharedWorkspace: SharedWorkspace): Future[SharedWorkspace] = Future {
    NamedDB('default) localTx { implicit session =>
      val sw = SharedWorkspace.column
      val id = applyUpdateAndReturnGeneratedKey {
        insert.into(SharedWorkspace)
          .columns(sw.name, sw.systemName, sw.purpose, sw.created, sw.createdBy, sw.requestedSize, sw.requestedCores, sw.requestedMemory, sw.complianceId)
          .values(sharedWorkspace.name, sharedWorkspace.systemName, sharedWorkspace.purpose, sharedWorkspace.created, sharedWorkspace.createdBy, sharedWorkspace.requestedSize, sharedWorkspace.requestedCores, sharedWorkspace.requestedMemory, sharedWorkspace.complianceId)
      }

      baseSql((sw, _, _, _, _) => _.eq(sw.id, id))
        .single()
        .apply()
        .get
    }
  }

  override def setLDAP(workspaceId: Long, ldapRegistrationId: Long): Future[SharedWorkspace] = Future {
    NamedDB('default) localTx { implicit session =>
      applyUpdate {
        update(SharedWorkspace)
          .set(
            SharedWorkspace.column.ldapRegistrationId -> ldapRegistrationId
          )
          .where.eq(SharedWorkspace.column.id, workspaceId)
      }

      baseSql((sw, _, _, _, _) => _.eq(sw.id, workspaceId))
        .single()
        .apply()
        .get
    }
  }

  override def setHive(workspaceId: Long, hiveDatbaseId: Long): Future[SharedWorkspace] = Future {
    NamedDB('default) localTx { implicit session =>
      applyUpdate {
        update(SharedWorkspace)
          .set(
            SharedWorkspace.column.hiveDatabaseId -> hiveDatbaseId
          )
          .where.eq(SharedWorkspace.column.id, workspaceId)
      }

      baseSql((sw, _, _, _, _) => _.eq(sw.id, workspaceId))
        .single()
        .apply()
        .get
    }
  }

  override def setYarn(workspaceId: Long, yarnId: Long): Future[SharedWorkspace] = Future {
    NamedDB('default) localTx { implicit session =>
      applyUpdate {
        update(SharedWorkspace)
          .set(
            SharedWorkspace.column.yarnId -> yarnId
          )
          .where.eq(SharedWorkspace.column.id, workspaceId)
      }

      baseSql((sw, _, _, _, _) => _.eq(sw.id, workspaceId))
        .single()
        .apply()
        .get
    }
  }
}
