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
    } yield savedWorkspaceRequest.copy(compliance = newCompliance)).transact(transactor)

    val newRecord = repository.create(newRequest.unsafeRunSync()).transact(transactor).unsafeRunSync()
    newRecord.id shouldBe defined

    sql"delete from approval".update.run.transact(transactor).unsafeRunSync
    sql"delete from workspace_request".update.run.transact(transactor).unsafeRunSync
    sql"delete from compliance".update.run.transact(transactor).unsafeRunSync
  }

}
