package io.phdata.provisioning

import java.io.File
import java.util.UUID

import cats.effect.{ContextShift, IO, Resource}
import io.phdata.AppContext
import io.phdata.clients.HiveClientImpl
import io.phdata.itest.fixtures.{IntegrationTest, KerberosTest}
import io.phdata.models.{Compliance, Metadata, DistinguishedName, WorkspaceRequest}
import io.phdata.services.UGILoginContextProvider
import io.phdata.test.fixtures.{TestTimer}
import com.typesafe.config.ConfigFactory
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers, _}

import scala.concurrent.ExecutionContext

class HiveDatabaseRegistrationIntegrationSpec
  extends FlatSpec
  with Matchers
  with MockFactory
  with BeforeAndAfterAll
  with KerberosTest
  with IntegrationTest {


  implicit def contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  implicit val timer = IO.timer(ExecutionContext.global)

  override def afterAll(): Unit = {
    new Context {
      context.use{case (_, hiveClient)  =>
        hiveClient.dropDatabase(hiveDatabaseRegistration.name)
      }
    }
  }

  behavior of "HiveDatabaseRegistration"

  it should "create a new hive database and invalidate impala metadata" in new Context {

    context.use { case (workspaceContext, hiveClient) =>
        for {
          _ <- HiveDatabaseRegistrationProvisioning.HiveDatabaseRegistrationProvisioningTask.run(hiveDatabaseRegistration, workspaceContext)
          _ <- hiveClient.dropDatabase(hiveDatabaseRegistration.name)
        } yield()

    }.unsafeRunSync()
  }

  trait Context {
    val workspaceId = 130L
    val FOO_DB_NAME = s"zz_heimdali_hive_db_registration_${UUID.randomUUID().toString.take(8)}"
    val hiveDatabaseRegistration = new HiveDatabaseRegistration(workspaceId, FOO_DB_NAME, "/tmp")

    val workspaceRequest = new WorkspaceRequest(
      "workspace_name",
      "workspace_summary",
      "workspace_description",
      "workspace_behavior",
      DistinguishedName("cn=john.doe,ou=hadoop,dc=example,dc=com"),
      new TestTimer().instant,
      new Compliance(false, false, false),
      false,
      metadata = Metadata("workspace_name", "workspace_description", 0, Map.empty)
    )

    def context: Resource[IO, (WorkspaceContext[IO], HiveClientImpl[IO])] = {
      val config = ConfigFactory.parseFile(new File("itest-config/application.itest.conf")).resolve()
      for {
        context <- AppContext.default[IO](config)
        loginCtxProvider = new UGILoginContextProvider(context.appConfig)
        hiveXA: doobie.Transactor[IO] = context.appConfig.db.hive.hiveTx
        workspaceContext = new WorkspaceContext[IO](workspaceId, context)
        hiveClient = new HiveClientImpl[IO](loginCtxProvider, hiveXA)
      } yield (workspaceContext, hiveClient)
    }
  }

}
