package com.heimdali.actors

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import com.heimdali.actors.HDFSActor.CreateUserDirectory
import com.heimdali.actors.HiveActor.{CreateUserDatabase, DatabaseCreated}
import com.heimdali.actors.LDAPActor.{CreateUserWorkspaceGroup, LDAPGroupCreated}
import com.heimdali.actors.UserSaver.{SaveUser, UserSaved}
import com.heimdali.actors.user.UserProvisioner
import com.heimdali.actors.user.UserProvisioner.Started
import com.heimdali.services.UserWorkspace
import org.scalamock.scalatest.MockFactory
import org.scalatest.FlatSpec

class UserProvisionerSpec extends FlatSpec with MockFactory {

  behavior of "User provisioning"

  it should "create user db" in new TestKit(ActorSystem()) with ImplicitSender {
    val user = UserWorkspace("username", HiveDatabase("", "", ""))
    val actor = system.actorOf(UserProvisioner.props(testActor, testActor, testActor, testActor, user))
    watch(actor)

    actor ! UserProvisioner.Request

    expectMsg(Started)
    expectMsg(CreateUserWorkspaceGroup("username"))
    expectMsg(CreateUserDirectory("username"))
    actor ! LDAPGroupCreated("group", "dn")
    actor ! DatabaseCreated(HiveDatabase("location", "role", "database"))

    expectMsg(CreateUserDatabase("username"))

    expectMsg(SaveUser(UserWorkspace("username", HiveDatabase("location", "role", "database"))))
    actor ! UserSaved
    expectTerminated(actor)
  }

}
