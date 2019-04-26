package com.heimdali

import cats.data._
import cats.effect._
import cats.implicits._
import com.heimdali.models._
import com.heimdali.provisioning.ProvisionTask._

package object provisioning {

  type WorkspaceContext[F[_]] = (Option[Long], AppContext[F])

  implicit def registrationProvisioner[F[_] : Effect : Timer]: ProvisionTask[F, LDAPRegistration] =
    ProvisionTask.instance { registration =>
      for {
        group <- CreateLDAPGroup(registration.id.get, registration.commonName, registration.distinguishedName, registration.attributes).provision[F]
        role <- CreateRole(registration.id.get, registration.sentryRole).provision[F]
        grant <- GrantGroupAccess(registration.id.get, registration.sentryRole, registration.commonName).provision[F]
      } yield role |+| group |+| grant
    }

  implicit def grantProvisioner[F[_] : Effect : Timer]: ProvisionTask[F, HiveGrant] =
    ProvisionTask.instance { grant =>
      for {
        ldap <- grant.ldapRegistration.provision[F]
        db <- GrantDatabaseAccess(grant.id.get, grant.ldapRegistration.sentryRole, grant.databaseName, grant.databaseRole).provision[F]
        location <- GrantLocationAccess(grant.id.get, grant.ldapRegistration.sentryRole, grant.location).provision[F]
      } yield ldap |+| db |+| location
    }

  implicit def hiveProvisioner[F[_] : Effect : Timer]: ProvisionTask[F, HiveAllocation] =
    ProvisionTask.instance { hive =>
      for {
        createDirectory <- CreateDatabaseDirectory(hive.id.get, hive.location, None).provision[F]
        setDiskQuota <- SetDiskQuota(hive.id.get, hive.location, hive.sizeInGB).provision[F]
        createDatabase <- CreateHiveDatabase(hive.id.get, hive.name, hive.location).provision[F]
        managers <- hive.managingGroup.provision[F]
        readwrite <- hive.readWriteGroup.map(_.provision[F]).getOrElse(Kleisli[F, WorkspaceContext[F], ProvisionResult](_ => Effect[F].liftIO(IO.pure(NoOp("hive database readwrite")))))
        readonly <- hive.readonlyGroup.map(_.provision[F]).getOrElse(Kleisli[F, WorkspaceContext[F], ProvisionResult](_ => Effect[F].liftIO(IO.pure(NoOp("hive database readonly")))))
      } yield createDirectory |+| setDiskQuota |+| createDatabase |+| managers |+| readwrite |+| readonly
    }

  implicit def appProvisioner[F[_] : Effect : Timer]: ProvisionTask[F, Application] =
    ProvisionTask.instance { app =>
      for {
        group <- app.group.provision[F]
        grant <- GrantRoleToConsumerGroup(app.id.get, app.consumerGroup, app.group.sentryRole).provision[F]
      } yield group |+| grant
    }

  implicit def topicProvisioner[F[_] : Effect : Timer]: ProvisionTask[F, KafkaTopic] =
    ProvisionTask.instance(topic =>
      for {
        create <- CreateKafkaTopic(topic.id.get, topic.name, topic.partitions, topic.replicationFactor).provision
        managingRole <- topic.managingRole.provision
        readonlyRole <- topic.readonlyRole.provision
      } yield create |+| managingRole |+| readonlyRole /* |+| manager */
    )

  implicit def topicGrantProvisioner[F[_] : Effect : Timer]: ProvisionTask[F, TopicGrant] =
    ProvisionTask.instance { grant =>
      for {
        ldap <- grant.ldapRegistration.provision
        access <- GrantTopicAccess(grant.id.get, grant.name, grant.ldapRegistration.sentryRole, NonEmptyList.fromListUnsafe(grant.actions.split(",").toList)).provision
      } yield access |+| ldap
    }

  implicit def yarnProvisioner[F[_] : Effect : Timer]: ProvisionTask[F, Yarn] =
    ProvisionTask.instance { yarn =>
      CreateResourcePool(yarn.id.get, yarn.poolName, yarn.maxCores, yarn.maxMemoryInGB).provision
    }

}
