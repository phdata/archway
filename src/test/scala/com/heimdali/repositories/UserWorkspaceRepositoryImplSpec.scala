package com.heimdali.repositories

import com.heimdali.models.UserWorkspace
import com.heimdali.test.fixtures._
import org.scalatest.{AsyncFlatSpec, Matchers}

class UserWorkspaceRepositoryImplSpec
  extends AsyncFlatSpec
    with Matchers
    with DBTest {

  behavior of "User Workspace Repository"

  it should "create a new workspace" in {
    val repo = new UserWorkspaceRepositoryImpl()
    repo.create(standardUsername) map { workspace =>
      workspace.username shouldBe standardUsername
    }
  }

  override val tables: Seq[scalikejdbc.SQLSyntaxSupport[_]] =
    Seq(UserWorkspace)
}
