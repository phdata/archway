package com.heimdali.test

import java.time.{Clock, Instant, ZoneId}

import cats.effect.IO
import com.heimdali.clients.CMClient
import org.http4s.HttpService
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.dsl.io._
import cats.effect.IO
import com.heimdali.clients.CMClient
import com.heimdali.config.{AppConfig, ClusterConfig, CredentialsConfig, LDAPConfig}
import com.heimdali.models._
import com.heimdali.services.{CDH, Cluster, ClusterApp}
import io.circe._
import io.circe.parser._
import org.http4s.HttpService
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.dsl.io._
import pureconfig.{CamelCase, ConfigFieldMapping, ProductHint}

import scala.concurrent.duration._
import scala.io.Source

package object fixtures {

  val personName = "John Doe"
  val standardUsername = "john.doe"
  val standardUserDN = s"cn=$standardUsername,ou=hadoop,dc=example,dc=com"
  val infraApproverToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJuYW1lIjoiRHVkZSBEb2UiLCJ1c2VybmFtZSI6InVzZXJuYW1lIiwiZGlzdGluZ3Vpc2hlZF9uYW1lIjoiY249am9obi5kb2Usb3U9aGFkb29wLGRjPWV4YW1wbGUsZGM9Y29tIiwicGVybWlzc2lvbnMiOnsicmlza19tYW5hZ2VtZW50IjpmYWxzZSwicGxhdGZvcm1fb3BlcmF0aW9ucyI6dHJ1ZX19.RSYFux7Ra7-qA9oICXn9L10UMZzrBxXxMr6oQck_oRtO8W3ev2u8vj57b-Kqpw-c0f_K7MTnLqpbfa45IDUg6Q"
  val infraApproverUser = User(personName, standardUsername, standardUserDN, UserPermissions(riskManagement = false, platformOperations = true))
  val basicUserToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJuYW1lIjoiRHVkZSBEb2UiLCJ1c2VybmFtZSI6InVzZXJuYW1lIiwicGVybWlzc2lvbnMiOnsicmlza19tYW5hZ2VtZW50IjpmYWxzZSwicGxhdGZvcm1fb3BlcmF0aW9ucyI6ZmFsc2V9fQ.ltGXxBh4S7gwmIbcKz22IFWpGI2-zxad2XYOoxuGm734L8GlzfwvLRWIs-ZVKn7T8w3RJy5bKZWZoPj8951Qug"
  val basicUser = User(personName, standardUsername, standardUserDN, UserPermissions(riskManagement = false, platformOperations = false))

  val id = 123L
  val name = "Sesame"
  val purpose = "World Peace"
  val phiCompliance = false
  val piiCompliance = false
  val pciCompliance = false
  val hdfsRequestedSize: Int = 250
  val actualGB: None.type = None
  val keytabLocation: Option[String] = None
  val systemName: String = "sesame"
  val hdfsLocation: String = s"/data/shared_workspaces/$systemName"
  val ldapDn: String = s"cn=edh_sw_$systemName,ou=hadoop,dc=example,dc=com"
  val environment = "dev"
  val maxCores = 4
  val maxMemoryInGB = 16
  val poolName: String = "root.workspaces.pool"
  val savedCompliance = Compliance(phiCompliance, pciCompliance, piiCompliance, Some(id))
  val initialCompliance = savedCompliance.copy(id = None)
  val savedLDAP = LDAPRegistration(ldapDn, s"edh_sw_$systemName", "role_sesame", Some(id))
  val initialLDAP = savedLDAP.copy(id = None)
  val savedGrant = HiveGrant("sw_sesame", "/shared_workspaces/sw_sesame", savedLDAP, id = Some(id))
  val initialGrant = savedGrant.copy(id = None, ldapRegistration = initialLDAP)
  val savedHive = HiveAllocation("sw_sesame", "/shared_workspaces/sw_sesame", hdfsRequestedSize, None, savedGrant, id = Some(id))
  val initialHive = savedHive.copy(id = None, managingGroup = initialGrant)
  val savedYarn = Yarn(poolName, maxCores, maxMemoryInGB, Some(id))
  val initialYarn = savedYarn.copy(id = None)
  val savedTopic = KafkaTopic(s"$systemName.incoming", 1, 1, TopicGrant(s"$systemName.incoming", savedLDAP, "all", Some(id)), TopicGrant(s"$systemName.incoming", savedLDAP, "read", Some(id)), Some(id))
  val initialTopic = savedTopic.copy(id = None, managingRole = savedTopic.managingRole.copy(id = None, ldapRegistration = initialLDAP), readonlyRole = savedTopic.readonlyRole.copy(id = None, ldapRegistration = initialLDAP))
  val savedApplication = Application("Tiller", s"${systemName}_cg", savedLDAP, Some(id), Some(standardUsername))
  val initialApplication = savedApplication.copy(id = None, group = initialLDAP)
  implicit val clock = Clock.fixed(Instant.now(), ZoneId.of("UTC"))

  val yarnApp = ClusterApp("yarn", "yarn", "GOOD_HExALTH", "STARTED", Map())
  val cluster = Cluster("cluster name", "Cluster", "", List(yarnApp), CDH(""), "GOOD_HEALTH")

  def approval(instant: Instant = Instant.now(clock)) = Approval(Risk, standardUsername, instant)

  val clusterConfig = ClusterConfig(1 second, "", "cluster", "dev", CredentialsConfig("admin", "admin"))
  private implicit def hint[T] = ProductHint[T](ConfigFieldMapping(CamelCase, CamelCase))
  val Right(appConfig) = pureconfig.loadConfig[AppConfig]

  val savedWorkspaceRequest = WorkspaceRequest(
    name,
    name,
    name,
    "simple",
    standardUsername,
    Instant.now(clock),
    savedCompliance,
    singleUser = false,
    id = Some(id),
    data = List(savedHive),
    processing = List(savedYarn),
    applications = List(savedApplication))

  val initialWorkspaceRequest =
    savedWorkspaceRequest.copy(
      id = None,
      compliance = initialCompliance,
      data = List(initialHive),
      processing = List(initialYarn),
      applications = List(initialApplication)
    )

  val Right(defaultRequest) = parse(
    s"""
       | {
       |   "name": "$name",
       |   "purpose": "$purpose",
       |   "compliance": {
       |     "pii_data": $piiCompliance,
       |     "phi_data": $phiCompliance,
       |     "pci_data": $pciCompliance
       |   }
       | }
      """.stripMargin)

  val Right(defaultResponse) = parse(
    s"""
       |{
       |  "id" : $id,
       |  "name" : "$name",
       |  "summary" : "$name",
       |  "description" : "$name",
       |  "behavior" : "simple",
       |  "single_user" : ${initialWorkspaceRequest.singleUser},
       |  "status": "Pending",
       |  "compliance" : {
       |    "phi_data" : ${savedCompliance.phiData},
       |    "pci_data" : ${savedCompliance.pciData},
       |    "pii_data" : ${savedCompliance.piiData}
       |  },
       |  "processing" : [
       |    {
       |      "pool_name" : "${savedYarn.poolName}",
       |      "max_cores" : ${savedYarn.maxCores},
       |      "max_memory_in_gb" : ${savedYarn.maxMemoryInGB}
       |    }
       |  ],
       |  "data" : [
       |    {
       |      "id" : ${savedHive.id.get},
       |      "name" : "${savedHive.name}",
       |      "location" : "${savedHive.location}",
       |      "size_in_gb" : ${savedHive.sizeInGB},
       |      "managing_group" : {
       |        "group": {
       |          "common_name" : "${savedLDAP.commonName}",
       |          "distinguished_name" : "${savedLDAP.distinguishedName}",
       |          "sentry_role" : "${savedLDAP.sentryRole}"
       |        }
       |      }
       |    }
       |  ],
       |  "applications": [
       |    {
       |      "id": ${savedApplication.id.get},
       |      "name": "${savedApplication.name}",
       |      "consumer_group": "${savedApplication.consumerGroup}",
       |      "group": {
       |          "common_name" : "${savedLDAP.commonName}",
       |          "distinguished_name" : "${savedLDAP.distinguishedName}",
       |          "sentry_role" : "${savedLDAP.sentryRole}"
       |      }
       |    }
       |  ],
       |  "topics": [],
       |  "single_user": false,
       |  "requester": "$standardUsername",
       |  "requested_date": "${Instant.now(clock)}"
       |}
       """.stripMargin)

  def fromResource(path: String): Json = {
    val json = Source.fromResource(path).getLines().mkString
    val Right(parsedJson) = parse(json)
    parsedJson
  }

  val testClient = IO.pure(Client.fromHttpService(HttpService[IO] {
    case GET -> Root / "api" / "v18" / "clusters" / "cluster" =>
      Ok(fromResource("cloudera/clusters.cluster_name.actual.json"))

    case GET -> Root / "api" / "v18" / "clusters" / "cluster" / "services" / serviceName / "roles" =>
      Ok(fromResource(s"cloudera/$serviceName.json"))

    case GET -> Root / "api" / "v18" / "hosts" =>
      Ok(fromResource("cloudera/hosts.json"))

    case GET -> Root / "api" / "v18" / "clusters" / "cluster" / "services" =>
      Ok(fromResource("cloudera/services.json"))
  }))

  val httpClient = new CMClient[IO](testClient, clusterConfig)

}
