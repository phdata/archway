package com.heimdali.provisioning

import akka.actor.{Actor, Props}
import akka.pattern.pipe
import com.heimdali.models.{HiveDatabase, LDAPRegistration, Workspace}
import com.heimdali.repositories.{HiveDatabaseRepository, LDAPRepository}

import scala.concurrent.ExecutionContext

object WorkspaceSaver {

  case class LDAPUpdate[A](id: A, ldapRegistration: LDAPRegistration)

  case class HiveUpdate[A](id: A, hiveDatabase: HiveDatabase)

  def apply[A, T <: Workspace[A]](workspaceRepository: WorkspaceRepository[A, T],
                                  ldapRepository: LDAPRepository,
                                  hiveDatabaseRepository: HiveDatabaseRepository)
                                 (implicit executionContext: ExecutionContext) =
    Props(classOf[WorkspaceSaver[A, T]], workspaceRepository, ldapRepository, hiveDatabaseRepository, executionContext)

}

class WorkspaceSaver[A, T <: Workspace[A]](workspaceRepository: WorkspaceRepository[A, T],
                                           ldapRepository: LDAPRepository,
                                           hiveDatabaseRepository: HiveDatabaseRepository)
                                          (implicit val executionContext: ExecutionContext)
  extends Actor {

  import WorkspaceSaver._

  override def receive: Receive = {
    case LDAPUpdate(id: A, ldap) =>
      (for {
        newLDAP <- ldapRepository.create(ldap)
        workspace <- workspaceRepository.setLDAP(id, newLDAP.id.get)
      } yield workspace)
        .pipeTo(sender())

    case HiveUpdate(id: A, hive) =>
      (for {
        newHive <- hiveDatabaseRepository.create(hive)
        workspace <- workspaceRepository.setHive(id, newHive.id.get)
      } yield workspace)
        .pipeTo(sender())
  }

}