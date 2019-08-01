package io.phdata.repositories

import io.phdata.models.{Infra, Risk}
import io.phdata.repositories.syntax.SqlSyntax
import io.phdata.test.fixtures._
import doobie.scalatest.IOChecker
import io.phdata.repositories.syntax.SqlSyntax
import org.scalatest.{FunSuite, Matchers}

class WorkspaceRequestRepositoryImplIntegrationSpec
  extends FunSuite
    with Matchers
    with DBTest
    with IOChecker {
  
  val repository = new WorkspaceRequestRepositoryImpl(SqlSyntax.defaultSyntax)
  val statements = new repository.DefaultStatements

  // Test failing because typecheck doesn't allow a bit as a boolean type
//  test("create") { check(statements.insert(initialWorkspaceRequest)) }
//  test("pending") { check(statements.pending(Risk)) }
//  test("infra") { check(statements.pending(Infra)) }
//  test("list") { check(statements.listQuery(standardUsername)) }
//  test("find") { check(statements.find(id)) }
//  test("unprovisioned") { check(statements.findUnprovisioned())}
//  test("markProvisioned") { check(statements.markProvisioned(1L, testTimer.instant))}

}
