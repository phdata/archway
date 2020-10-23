package io.phdata.clients

import cats.effect.{Effect, Sync}
import cats.implicits._
import com.typesafe.scalalogging.LazyLogging
import doobie._
import doobie.implicits._
import io.phdata.models._
import io.phdata.repositories.CustomLogHandler
import io.phdata.services.LoginContextProvider
import org.apache.sentry.provider.db.generic.service.thrift.{SentryGenericServiceClient, TSentryPrivilege}

sealed trait Component {

  def name: String

  def privilege(grantString: String): TSentryPrivilege

}

case object Hive extends Component {
  val name: String = "hive"

  def privilege(grantString: String): TSentryPrivilege = ???
}

trait SentryClient[F[_]] {
  def createRole(name: String): F[Unit]

  def dropRole(role: String): F[Unit]

  def grantGroup(group: String, role: String): F[Unit]

  def revokeGroup(group: String, role: String): F[Unit]

  def groupRoles(group: String): F[List[String]]

  def enableAccessToDB(database: String, role: String, databaseRole: DatabaseRole): F[Unit]

  def removeAccessToDB(database: String, role: String, databaseRole: DatabaseRole): F[Unit]

  def showRoleGrants(role: String): F[List[DatabaseGrant]]

  def enableAccessToLocation(location: String, role: String): F[Unit]

  def removeAccessToLocation(location: String, role: String): F[Unit]

  def grantPrivilege(role: String, component: Component, grantString: String): F[Unit]

  def removePrivilege(role: String, component: Component, grantString: String): F[Unit]
}

class SentryClientImpl[F[_]](
    transactor: Transactor[F],
    client: SentryGenericServiceClient,
    loginContextProvider: LoginContextProvider
)(implicit val F: Effect[F])
    extends SentryClient[F] with LazyLogging {

  implicit val han: LogHandler = CustomLogHandler.logHandler(this.getClass)

  def roles: F[Seq[String]] = sql"""SHOW ROLES""".query[String].to[Seq].transact(transactor)

  override def createRole(name: String): F[Unit] =
    loginContextProvider.hadoopInteraction {
      roles.flatMap {
        case roles if !roles.contains(name) =>
          logger.info(s"Creating role $name")
          (fr"CREATE ROLE" ++ Fragment.const(name)).update.run.transact(transactor).void
        case _ =>
          Sync[F].unit
      }
    }

  override def dropRole(role: String): F[Unit] =
    loginContextProvider.hadoopInteraction {
      roles.flatMap {
        case roles if roles.contains(role) =>
          logger.info(s"Dropping role $role")
          (fr"DROP ROLE" ++ Fragment.const(role)).update.run.transact(transactor).void
        case _ =>
          logger.info(s"$role does not need to be deleted")
          Sync[F].unit
      }
    }

  override def grantGroup(group: String, role: String): F[Unit] = {
    logger.info(s"Granting role $role to group $group")
    loginContextProvider.hadoopInteraction {
      (fr"GRANT ROLE" ++ Fragment.const(role) ++ fr"TO GROUP" ++ Fragment.const(group)).update.run
        .transact(transactor)
        .flatMap(_ => Sync[F].unit)
    }
  }

  override def revokeGroup(group: String, role: String): F[Unit] = {
    logger.info(s"Revoking role $role from group $group")
    loginContextProvider.hadoopInteraction {
      (fr"REVOKE ROLE" ++ Fragment.const(role) ++ fr"FROM GROUP" ++ Fragment.const(group)).update.run
        .transact(transactor)
        .flatMap(_ => Sync[F].unit)
    }
  }

  override def groupRoles(group: String): F[List[String]] = {
    logger.info(s"Listing all roles for group $group")
    loginContextProvider.hadoopInteraction {
      (fr"SHOW ROLE GRANT GROUP" ++ Fragment.const(group)).query[String].to[List].transact(transactor)
    }
  }

  override def enableAccessToDB(database: String, role: String, databaseRole: DatabaseRole): F[Unit] = {
    logger.debug(s"Granting $databaseRole to $database for role $role").pure[F] *>
      loginContextProvider.hadoopInteraction {
        (databaseRole match {
          case Manager =>
            logger.info(s"Enabling full access to database $database to role $role")
            fr"GRANT ALL ON DATABASE" ++ Fragment.const(database) ++ fr"TO ROLE" ++ Fragment.const(role) ++ fr"WITH GRANT OPTION"
          case ReadWrite =>
            logger.info(s"Enabling readWrite access to database $database to role $role")
            fr"GRANT ALL ON DATABASE" ++ Fragment.const(database) ++ fr"TO ROLE" ++ Fragment.const(role)
          case ReadOnly =>
            logger.info(s"Enabling read only access to database $database to role $role")
            fr"GRANT SELECT ON DATABASE" ++ Fragment.const(database) ++ fr"TO ROLE" ++ Fragment.const(role)
        }).update.run.transact(transactor).flatMap(_ => Sync[F].unit)
      }
  }

  override def removeAccessToDB(database: String, role: String, databaseRole: DatabaseRole): F[Unit] =
    loginContextProvider.hadoopInteraction {
      (databaseRole match {
        case Manager | ReadWrite =>
          logger.info(s"Revoking full access on database $database from role $role")
          fr"REVOKE ALL ON DATABASE" ++ Fragment.const(database) ++ fr"FROM ROLE" ++ Fragment.const(role)
        case ReadOnly =>
          logger.info(s"Revoking read only access on database $database from role $role")
          fr"REVOKE SELECT ON DATABASE" ++ Fragment.const(database) ++ fr"FROM ROLE" ++ Fragment.const(role)
      }).update.run.transact(transactor).flatMap(_ => Sync[F].unit)
    }

  override def showRoleGrants(role: String): F[List[DatabaseGrant]] = {
    logger.info(s"Listing all the grants for a role $role")
    loginContextProvider.hadoopInteraction {
      (fr"SHOW GRANT ROLE" ++ Fragment.const(role)).query[DatabaseGrant].to[List].transact(transactor)
    }
  }

  override def enableAccessToLocation(location: String, role: String): F[Unit] = {
    logger.info(s"Enabling full access to location $location to role $role")
    loginContextProvider.hadoopInteraction {
      (fr"GRANT ALL ON URI '" ++ Fragment.const(location) ++ fr"' TO ROLE" ++ Fragment.const(role) ++ fr"WITH GRANT OPTION").update.run
        .transact(transactor)
        .flatMap(_ => Sync[F].unit)
    }
  }

  override def removeAccessToLocation(location: String, role: String): F[Unit] = {
    logger.info(s"Revoking access on $location from role $role")
    loginContextProvider.hadoopInteraction {
      (fr"REVOKE ALL ON URI '" ++ Fragment.const(location) ++ fr"' FROM ROLE" ++ Fragment.const(role)).update.run
        .transact(transactor)
        .flatMap(_ => Sync[F].unit)
    }
  }

  override def grantPrivilege(role: String, component: Component, grantString: String): F[Unit] = {
    logger.info(s"Granting privilege $grantString to role $role for component $component")
    loginContextProvider.hadoopInteraction {
      F.delay(
          client.grantPrivilege("archway", component.name, component.name, component.privilege(grantString))
        )
        .void
    }
  }

  override def removePrivilege(role: String, component: Component, grantString: String): F[Unit] = {
    logger.info(s"Removing privilege $grantString from role $role for component $component")
    loginContextProvider.hadoopInteraction {
      F.delay(
          client.dropPrivilege("archway", component.name, component.privilege(grantString))
        )
        .void
    }
  }
}
