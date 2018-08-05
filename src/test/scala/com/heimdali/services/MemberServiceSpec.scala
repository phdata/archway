package com.heimdali.services

import cats.implicits._
import cats.effect.IO
import com.heimdali.clients.LDAPClient
import com.heimdali.repositories.{LDAPRepository, Manager, MemberRepository, MemberRightsRecord}
import doobie.implicits._
import doobie._
import com.heimdali.models.{MemberRights, WorkspaceMember, WorkspaceMemberEntry}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}
import com.heimdali.test.fixtures._

class MemberServiceSpec extends FlatSpec with Matchers with DBTest with MockFactory {

  behavior of "Member Service"

  it should "list members" in new Context {
    val dataPermissions = MemberRightsRecord("data", standardUsername, systemName, id, Manager)
    memberRepository.list _ expects id returning List(dataPermissions).pure[ConnectionIO]

    val members = memberService.members(id).unsafeRunSync()

    members shouldBe List(WorkspaceMemberEntry(standardUsername, List(MemberRights(systemName, id, Manager)), List.empty, List.empty, List.empty))
  }

  trait Context {

    val memberRepository = mock[MemberRepository]
    val ldapRepository = mock[LDAPRepository]
    val ldapClient = mock[LDAPClient[IO]]

    val memberService =
      new MemberServiceImpl[IO](
        memberRepository,
        transactor,
        ldapRepository,
        ldapClient
      )

  }

}
