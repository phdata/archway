package com.heimdali.actors

import akka.actor.{ActorSystem, Props}
import akka.testkit.TestProbe
import com.heimdali.actors.LDAPActor.{CreateSharedWorkspaceGroup, LDAPUpdate}
import com.heimdali.services.LDAPClient
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.Future

class LDAPActorSpec extends FlatSpec with Matchers with MockFactory {

  behavior of "LDAP Actor"

  it should "create a group when asked" in {
    val (projectName, username) = ("sesame", "username")

    implicit val actorSystem = ActorSystem()
    val probe = TestProbe()

    val ldapClient = mock[LDAPClient]
    implicit val executionContext = scala.concurrent.ExecutionContext.global
    (ldapClient.createGroup _).expects(projectName, username).returning(Future { "" })

    val actor = actorSystem.actorOf(Props(classOf[LDAPActor], ldapClient, executionContext))
    val project = CreateSharedWorkspaceGroup(123, projectName, Seq(username))
    actor.tell(project, probe.ref)
    probe.expectMsgClass(classOf[LDAPUpdate])
  }

}
