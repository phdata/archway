package com.heimdali.repositories

import java.time.Clock

import cats.effect.IO
import com.heimdali.test.fixtures._
import doobie.implicits._
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

class SharedWorkspaceRequestRepositorySpec extends FlatSpec with Matchers with DBTest with BeforeAndAfterAll {
  var newComplianceId: Long = _

  behavior of "Account Repository"

  it should "Save and extract a record just fine" in {
    val repository = new WorkspaceRequestRepositoryImpl

    val newRequest = (for {
      newCompliance <- new ComplianceRepositoryImpl().create(savedCompliance)
      newLdap <- new LDAPRepositoryImpl(Clock.systemUTC()).create(savedLDAP)
      newHive <- new HiveDatabaseRepositoryImpl(Clock.systemUTC()).create(savedHive.copy(managingGroup = newLdap))
      newYarn <- new YarnRepositoryImpl().create(savedYarn)
    } yield savedWorkspaceRequest.copy(
      compliance = newCompliance,
      data = List(newHive),
      processing = List(newYarn))).transact(transactor)

    val newRecord = repository.create(newRequest.unsafeRunSync()).transact(transactor).unsafeRunSync()
    newRecord.id shouldBe defined

    sql"delete from approval".update.run.transact(transactor).unsafeRunSync
    sql"delete from request_hive".update.run.transact(transactor).unsafeRunSync
    sql"delete from request_yarn".update.run.transact(transactor).unsafeRunSync
    sql"delete from workspace_request".update.run.transact(transactor).unsafeRunSync
    sql"delete from yarn".update.run.transact(transactor).unsafeRunSync
    sql"delete from hive_database".update.run.transact(transactor).unsafeRunSync
    sql"delete from compliance".update.run.transact(transactor).unsafeRunSync
    sql"delete from member".update.run.transact(transactor).unsafeRunSync
    sql"delete from ldap_registration".update.run.transact(transactor).unsafeRunSync
  }

}
