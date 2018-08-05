package com.heimdali.test

import java.time.{ Clock, Instant, ZoneId }

import com.heimdali.config.{ClusterConfig, CredentialsConfig}
import com.heimdali.models._
import com.heimdali.services.{BasicClusterApp, CDH, Cluster}
import io.circe.parser._
import java.util.TimeZone
import org.joda.time.DateTime
import scala.concurrent.duration._

package object fixtures {
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
  val savedHive = HiveDatabase("sw_sesame", "/shared_workspaces/sw_sesame", hdfsRequestedSize, savedGrant, id = Some(id))
  val initialHive = savedHive.copy(id = None, managingGroup = initialGrant)
  val savedYarn = Yarn(poolName, maxCores, maxMemoryInGB, Some(id))
  val initialYarn = savedYarn.copy(id = None)
  val savedTopic = KafkaTopic(s"$systemName.incoming", 1, 1, TopicGrant(s"$systemName.incoming", savedLDAP, "all", Some(id)), TopicGrant(s"$systemName.incoming", savedLDAP, "read", Some(id)), Some(id))
  val initialTopic = savedTopic.copy(id = None, managingRole = savedTopic.managingRole.copy(id = None, ldapRegistration = initialLDAP), readonlyRole = savedTopic.readonlyRole.copy(id = None, ldapRegistration = initialLDAP))
  val savedApplication = Application("Tiller", s"${systemName}_cg", savedLDAP, Some(id))
  val initialApplication = savedApplication.copy(id = None, group = initialLDAP)
  val clock = Clock.fixed(Instant.now(), ZoneId.of("UTC"))

  val yarnApp = BasicClusterApp("ysr21", "Yarn", "GOOD_HExALTH", "STARTED")
  val cluster = Cluster("cluster name", "Cluster", Map("YARN" -> yarnApp), CDH(""), "GOOD_HEALTH")

  val personName = "John Doe"
  val standardUsername = "john.doe"
  val infraApproverToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJuYW1lIjoiRHVkZSBEb2UiLCJ1c2VybmFtZSI6InVzZXJuYW1lIiwicGVybWlzc2lvbnMiOnsicmlza19tYW5hZ2VtZW50IjpmYWxzZSwicGxhdGZvcm1fb3BlcmF0aW9ucyI6dHJ1ZX19.uNJ0uQevSRL8aqYqmfsijVoj3gjlHbk07XBwGSXRuOboA1zkcMHhmHhiKVmFW_AVwDvqVdYAJ-XpNp7qTMHuQg"
  val infraApproverUser = User(personName, standardUsername, UserPermissions(riskManagement = false, platformOperations = true))
  val basicUserToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJuYW1lIjoiRHVkZSBEb2UiLCJ1c2VybmFtZSI6InVzZXJuYW1lIiwicGVybWlzc2lvbnMiOnsicmlza19tYW5hZ2VtZW50IjpmYWxzZSwicGxhdGZvcm1fb3BlcmF0aW9ucyI6ZmFsc2V9fQ.ltGXxBh4S7gwmIbcKz22IFWpGI2-zxad2XYOoxuGm734L8GlzfwvLRWIs-ZVKn7T8w3RJy5bKZWZoPj8951Qug"
  val basicUser = User(personName, standardUsername, UserPermissions(riskManagement = false, platformOperations = false))

  def approval(instant: Instant = Instant.now(clock)) = Approval(Risk, standardUsername, instant)

  val clusterConfig = ClusterConfig(1 second, "", "cluster name", "dev", CredentialsConfig("admin", "admin"))

  val savedWorkspaceRequest = WorkspaceRequest(
    name,
    standardUsername,
    Instant.now(clock),
    savedCompliance,
    singleUser = false,
    id = Some(id),
    data = List(savedHive),
    processing = List(savedYarn))

  val initialWorkspaceRequest =
    savedWorkspaceRequest.copy(
      id = None,
      compliance = initialCompliance,
      data = List(initialHive),
      processing = List(initialYarn)
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
       |  "single_user" : ${initialWorkspaceRequest.singleUser},
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
       |      "id" : "${id}",
       |      "name" : "${savedHive.name}",
       |      "location" : "${savedHive.location}",
       |      "size_in_gb" : ${savedHive.sizeInGB},
       |      "managing_group" : {
       |        "common_name" : "${savedLDAP.commonName}",
       |        "distinguished_name" : "${savedLDAP.distinguishedName}",
       |        "sentry_role" : "${savedLDAP.sentryRole}"
       |      }
       |    }
       |  ],
       |  "applications": [],
       |  "topics": [],
       |  "single_user": false,
       |  "requester": "$standardUsername",
       |  "requested_date": "${Instant.now(clock)}"
       |}
       """.stripMargin
  )

}
