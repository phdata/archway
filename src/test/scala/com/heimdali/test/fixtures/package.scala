package com.heimdali.test

import com.heimdali.models._
import com.heimdali.services.{BasicClusterApp, CDH, Cluster}
import io.circe.parser._
import org.joda.time.DateTime

package object fixtures {

  val id = 123L
  val name = "Sesame"
  val purpose = "World Peace"
  val ldapDn: String = s"cn=edh_sw_$systemName,ou=hadoop,dc=example,dc=com"
  val phiCompliance = false
  val piiCompliance = false
  val pciCompliance = false
  val hdfsLocation: String = s"/data/shared_workspaces/$systemName"
  val hdfsRequestedSize: Int = 250
  val actualGB: None.type = None
  val keytabLocation: Option[String] = None
  val systemName: String = SharedWorkspace.generateName(name)
  val environment = "dev"
  val maxCores = 4
  val maxMemoryInGB = 16
  val poolName: String = "pool"
  val compliance = Compliance(None, phiCompliance, pciCompliance, piiCompliance)
  val hive = HiveDatabase(None, "", "", "", hdfsRequestedSize)
  val yarn = Yarn(None, poolName, maxCores, maxMemoryInGB)
  val ldap = LDAPRegistration(None, ldapDn, s"edh_sw_$systemName")

  val yarnApp = BasicClusterApp("ysr21", "Yarn", "GOOD_HEALTH", "STARTED")
  val cluster = Cluster("cluster name", "Cluster", Map("YARN" -> yarnApp), CDH(""), "GOOD_HEALTH")

  val standardUsername = "john.doe"

  val initialSharedWorkspace = SharedWorkspace(
    Some(id),
    name,
    systemName,
    purpose,
    new DateTime(),
    standardUsername,
    hdfsRequestedSize,
    maxCores,
    maxMemoryInGB,
    None,
    Some(Compliance(None, phiCompliance, pciCompliance, piiCompliance))
  )

  val userWorkspace = UserWorkspace(
    standardUsername
  )

  val completedUserWorkspace = UserWorkspace(
    standardUsername,
    Some(123),
    Some(LDAPRegistration(Some(123), s"cn=user_$standardUsername,ou=hadoop,dc=example,dc=com", s"user_$standardUsername")),
    Some(123),
    Some(HiveDatabase(Some(123), "database", "role", "location", 1))
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
       |   },
       |   "requested_size_in_gb": $hdfsRequestedSize,
       |   "requested_cores": $maxCores,
       |   "requested_memory_in_gb": $maxMemoryInGB
       | }
      """.stripMargin)

  val Right(defaultResponse) = parse(
    s"""
       |{
       |  "id" : $id,
       |  "name" : "$name",
       |  "purpose" : "$purpose",
       |  "system_name" : "$systemName",
       |  "compliance" : {
       |    "phi_data" : $phiCompliance,
       |    "pci_data" : $pciCompliance,
       |    "pii_data" : $piiCompliance
       |  },
       |   "requested_size_in_gb": $hdfsRequestedSize,
       |   "requested_cores": $maxCores,
       |   "requested_memory_in_gb": $maxMemoryInGB,
       |  "created" : "20180423T112115.025-0500",
       |  "created_by" : "$standardUsername"
       |}
       """.stripMargin
  )

}
