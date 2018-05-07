package com.heimdali.provisioning

import akka.actor.{ActorSystem, Props}
import akka.testkit.TestProbe
import com.heimdali.clients.{LDAPClient, LDAPUser}
import com.heimdali.provisioning.LDAPActor.{CreateGroup, LDAPGroupCreated}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}
import com.heimdali.test.fixtures._

import scala.concurrent.Future
class LDAPActorSpec extends FlatSpec with Matchers with MockFactory {

  behavior of "LDAP Actor"

  it should "create a group when asked" in {

    val (projectName, username) = ("sesame", "username")

    implicit val actorSystem = ActorSystem()
    val probe = TestProbe()

    val ldapClient = mock[LDAPClient]
    implicit val executionContext = scala.concurrent.ExecutionContext.global
    ldapClient.createGroup _ expects initialSharedWorkspace.groupName returning Future("")
    ldapClient.addUser _ expects(initialSharedWorkspace.groupName, "username") returning Future(LDAPUser("", "", Seq.empty))

    val actor = actorSystem.actorOf(Props(classOf[LDAPActor], ldapClient, executionContext))
    val project = CreateGroup(initialSharedWorkspace.groupName, Seq(username))
    actor.tell(project, probe.ref)
    probe.expectMsgClass(classOf[LDAPGroupCreated])
  }

}
