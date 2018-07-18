package com.heimdali.repositories

import com.heimdali.test.fixtures._
import doobie.scalatest.IOChecker
import org.scalatest.{FunSuite, Matchers}

class SharedWorkspaceRequestRepositorySpec extends FunSuite with Matchers with DBTest with IOChecker {

  test("list") { check(WorkspaceRequestRepositoryImpl.Statements.listQuery(standardUsername)) }
  test("find") { check(WorkspaceRequestRepositoryImpl.Statements.find(id))}
  test("create") { check(WorkspaceRequestRepositoryImpl.Statements.insert(initialWorkspaceRequest))}

}
