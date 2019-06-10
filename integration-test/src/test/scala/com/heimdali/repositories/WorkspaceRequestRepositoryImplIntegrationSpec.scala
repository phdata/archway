package com.heimdali.repositories

import com.heimdali.common.IntegrationTest
import com.heimdali.models.{Infra, Risk}
import com.heimdali.test.fixtures._
import doobie.scalatest.IOChecker
import org.scalatest.{FunSuite, Matchers}


class WorkspaceRequestRepositoryImplIntegrationSpec
  extends FunSuite
    with Matchers
    with DBTest
    with IOChecker
    with IntegrationTest {

  test("list") { check(WorkspaceRequestRepositoryImpl.Statements.listQuery(standardUsername)) }
  test("find") { check(WorkspaceRequestRepositoryImpl.Statements.find(id)) }
  test("create") { check(WorkspaceRequestRepositoryImpl.Statements.insert(initialWorkspaceRequest)) }
  test("pending") { check(WorkspaceRequestRepositoryImpl.Statements.pending(Risk)) }
  test("infra") { check(WorkspaceRequestRepositoryImpl.Statements.pending(Infra)) }
  test("unprovisioned") { check(WorkspaceRequestRepositoryImpl.Statements.findUnprovisioned())}
  test("markProvisioned") { check(WorkspaceRequestRepositoryImpl.Statements.markProvisioned(1L, testTimer.instant))}

}
