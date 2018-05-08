package com.heimdali.provisioning

import akka.actor.ActorSystem
import akka.testkit.TestProbe
import com.heimdali.clients.{LDAPClient, LDAPUser}
import com.heimdali.provisioning.LDAPActor.{CreateGroup, LDAPGroupCreated}
import com.heimdali.test.fixtures._
import com.typesafe.config.Config
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
class LDAPActorSpec extends FlatSpec with Matchers with MockFactory {

  behavior of "LDAP Actor"

  it should "create a group when asked" in {

    val (projectName, username) = ("sesame", "username")

    implicit val actorSystem = ActorSystem()
    val probe = TestProbe()

    val configuration = mock[Config]
    val ldapClient = mock[LDAPClient]
    ldapClient.createGroup _ expects initialSharedWorkspace.groupName(configuration) returning Future("")
    ldapClient.addUser _ expects(initialSharedWorkspace.groupName(configuration), "username") returning Future(LDAPUser("", "", Seq.empty))

    val actor = actorSystem.actorOf(LDAPActor.props(ldapClient))
    val project = CreateGroup(initialSharedWorkspace.groupName(configuration), Seq(username))
    actor.tell(project, probe.ref)
    probe.expectMsgClass(classOf[LDAPGroupCreated])
  }

}
