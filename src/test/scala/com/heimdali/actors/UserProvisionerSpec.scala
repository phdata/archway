package com.heimdali.actors

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import com.heimdali.actors.HiveActor.{CreateUserDatabase, UserDatabaseCreated}
import com.heimdali.actors.LDAPActor.{CreateUserWorkspaceGroup, LDAPGroupCreated}
import com.heimdali.actors.user.UserProvisioner.Started
import com.heimdali.actors.UserSaver.{SaveUser, UserSaved}
import com.heimdali.actors.user.UserProvisioner
import com.heimdali.services.UserWorkspace
import org.scalamock.scalatest.MockFactory
import org.scalatest.FlatSpec

class UserProvisionerSpec extends FlatSpec with MockFactory {

  behavior of "User provisioning"

  it should "create user db" in new TestKit(ActorSystem()) with ImplicitSender {
    val user = UserWorkspace("username", "", "", "")
    val actor = system.actorOf(UserProvisioner.props(testActor, testActor, testActor, user))
    watch(actor)

    actor ! UserProvisioner.Request

    expectMsg(Started)
    expectMsg(CreateUserWorkspaceGroup("username"))
    actor ! LDAPGroupCreated("group", "dn")
    expectMsg(CreateUserDatabase("username"))
    actor ! UserDatabaseCreated(HiveDatabase("location", "role", "database"))

    expectMsg(SaveUser(UserWorkspace("username", "database", "location", "role")))
    actor ! UserSaved
    expectTerminated(actor)
  }

}
