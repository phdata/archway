package com.heimdali.services

import java.io.File

import cats.effect._
import com.heimdali.AppContext
import com.heimdali.clients._
import com.heimdali.generators._
import com.heimdali.repositories._
import com.heimdali.test.fixtures.{DBTest, _}
import com.typesafe.config.ConfigFactory
import doobie.util.ExecutionContexts
import org.apache.hadoop.conf.Configuration
import org.apache.sentry.provider.db.generic.service.thrift.SentryGenericServiceClientFactory
import org.http4s.client.blaze.BlazeClientBuilder
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class WorkspaceServiceIntegrationSpec extends FlatSpec with HiveTest with DBTest with Matchers {

  behavior of "Workspace integration spec"

  it should "find non-duplicated records" in new Context {
    implicit val timer = IO.timer(ExecutionContext.global)
    val endall = createService.use { case (service, generator) =>
      for {
        mgr <- generator.generate("edh_sw_mgr", s"edh_sw_mgr,$ldapDn", "role_edh_sw_mgr", initialWorkspaceRequest)
        rw <- generator.generate("edh_sw_rw", s"edh_sw_rw,$ldapDn", "role_edh_sw_rw", initialWorkspaceRequest)
        ro <- generator.generate("edh_sw_ro", s"edh_sw_ro,$ldapDn", "role_edh_sw_ro", initialWorkspaceRequest)
        workspace = initialWorkspaceRequest.copy(data = List(initialHive.copy(managingGroup = initialGrant.copy(ldapRegistration = mgr), readonlyGroup = Some(initialGrant.copy(ldapRegistration = ro)), readWriteGroup = Some(initialGrant.copy(ldapRegistration = rw)))))
        newWorkspace <- service.create(workspace)
        result <- service.find(newWorkspace.id.get).value
      } yield result
    }.unsafeRunSync()
    val managerAttributes = endall.get.data.head.managingGroup.ldapRegistration.attributes
    val readwriteAttributes = endall.get.data.head.readWriteGroup.get.ldapRegistration.attributes
    val readOnlyAttributes = endall.get.data.head.readonlyGroup.get.ldapRegistration.attributes

    managerAttributes.distinct.length shouldBe managerAttributes.length
    managerAttributes.map(_._2.replace("edh_sw_", "")) should contain noneOf("rw", "ro")

    readwriteAttributes.distinct.length shouldBe readwriteAttributes.length
    readwriteAttributes.map(_._2.replace("edh_sw_", "")) should contain noneOf("mgr", "ro")

    readOnlyAttributes.distinct.length shouldBe readOnlyAttributes.length
    readOnlyAttributes.map(_._2.replace("edh_sw_", "")) should contain noneOf("rw", "mgr")
  }

  trait Context {

    def createService(implicit timer: Timer[IO]): Resource[IO, (WorkspaceService[IO], LDAPGroupGenerator[IO])] =
      for {
        context <- AppContext.default[IO](ConfigFactory.parseResources("application.test.conf").resolve())
        configService = new DBConfigService[IO](context)
        ldapGroupGenerator = LDAPGroupGenerator.instance(context.appConfig, configService, context.appConfig.templates.ldapGroupGenerator)
        workspaceService = new WorkspaceServiceImpl[IO](null, context)
      } yield (workspaceService, ldapGroupGenerator)
  }

}
