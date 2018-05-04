package com.heimdali.repositories

import com.heimdali.models._
import scalikejdbc._

import scala.concurrent.{ExecutionContext, Future}

class UserWorkspaceRepositoryImpl(implicit executionContext: ExecutionContext)
  extends UserWorkspaceRepository with SQLSyntaxSupport[UserWorkspace] {

  override val tableName: String = "users"

  def baseSql(insidePart: (QuerySQLSyntaxProvider[_, _], QuerySQLSyntaxProvider[_, _], QuerySQLSyntaxProvider[_, _], QuerySQLSyntaxProvider[_, _]) => (ConditionSQLBuilder[_] => ConditionSQLBuilder[_]))(implicit session: DBSession): SQL[UserWorkspace, HasExtractor] = {
    val uw = UserWorkspace.syntax
    val l = LDAPRegistration.syntax
    val h = HiveDatabase.syntax
    val y = Yarn.syntax
    withSQL {
      select
        .from(UserWorkspace as uw)
        .leftJoin(LDAPRegistration as l).on(uw.ldapRegistrationId, l.id)
        .leftJoin(HiveDatabase as h).on(uw.hiveDatabaseId, h.id)
        .leftJoin(Yarn as y).on(uw.yarnId, y.id)
        .where.withRoundBracket(insidePart(uw, l, h, y))
    }
      .map(UserWorkspace(uw.resultName, l.resultName, h.resultName, y.resultName))
  }

  override def findUser(username: String): Future[Option[UserWorkspace]] = Future {
    NamedDB('default) readOnly { implicit session =>
      baseSql((uw, _, _, _) => _.eq(uw.username, username))
        .single().apply()
    }
  }

  override def create(username: String): Future[UserWorkspace] = Future {
    NamedDB('default) autoCommit { implicit session =>
      val u = UserWorkspace.syntax
      val uc = UserWorkspace.column
      val existing = withSQL {
        select.from(UserWorkspace as u)
          .where.eq(u.username, username)
      }.map(_.stringOpt(u.username)).single().apply()

      if(existing isEmpty) {
        applyUpdate {
          insert.into(UserWorkspace)
            .namedValues(
              uc.username -> username
            )
        }
      }

      baseSql((uw, _, _, _) => _.eq(uw.username, username))
        .single().apply().get
    }
  }

  override def setLDAP(username: String, ldapRegistrationId: Long): Future[UserWorkspace] = Future {
    NamedDB('default) localTx { implicit session =>
      applyUpdate {
        update(UserWorkspace)
          .set(
            UserWorkspace.column.ldapRegistrationId -> ldapRegistrationId
          )
          .where.eq(UserWorkspace.column.username, username)
      }

      baseSql((uw, _, _, _) => _.eq(uw.username, username))
        .single().apply().get
    }
  }

  override def setHive(username: String, hiveDatbaseId: Long): Future[UserWorkspace] = Future {
    NamedDB('default) localTx { implicit session =>
      applyUpdate {
        update(UserWorkspace)
          .set(
            UserWorkspace.column.hiveDatabaseId -> hiveDatbaseId
          )
          .where.eq(UserWorkspace.column.username, username)
      }

      baseSql((uw, _, _, _) => _.eq(uw.username, username))
        .single().apply().get
    }
  }

}