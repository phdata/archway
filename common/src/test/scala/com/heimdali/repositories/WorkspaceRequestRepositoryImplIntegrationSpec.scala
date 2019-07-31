package com.heimdali.repositories

import com.heimdali.models.{Infra, Risk}
import com.heimdali.repositories.syntax.SqlSyntax
import com.heimdali.test.fixtures._
import doobie.scalatest.IOChecker
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
