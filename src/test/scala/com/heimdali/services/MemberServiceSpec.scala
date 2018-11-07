package com.heimdali.services

import cats.data.OptionT
import cats.implicits._
import cats.effect.IO
import com.heimdali.clients.{LDAPClient, LDAPUser}
import com.heimdali.repositories.{LDAPRepository, Manager, MemberRepository, MemberRightsRecord}
import doobie.implicits._
import doobie._
import com.heimdali.models.{MemberRights, WorkspaceMember, WorkspaceMemberEntry}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}
import com.heimdali.test.fixtures._
import com.unboundid.ldap.sdk.{Attribute, Control, SearchResultEntry}

class MemberServiceSpec extends FlatSpec with Matchers with DBTest with MockFactory {

  behavior of "Member Service"

  it should "list members" in new Context {
    val dataPermissions = MemberRightsRecord("data", standardUsername, systemName, id, Manager)
    memberRepository.list _ expects id returning List(dataPermissions).pure[ConnectionIO]
    ldapClient.findUser _ expects standardUsername returning OptionT.some(LDAPUser(personName, standardUsername, standardUserDN, Seq.empty, None))

    val members = memberService.members(id).unsafeRunSync()

    members shouldBe List(WorkspaceMemberEntry(standardUsername, personName, None, List(MemberRights(systemName, id, Manager)), List.empty, List.empty, List.empty))
  }

  it should "find members" in new Context {
    ldapClient.search _ expects "joh" returning IO.pure(List(
      new SearchResultEntry("cn=John,dc=example,dc=io", Array(new Attribute("cn", "John"), new Attribute("objectClass", "user")))
    ))

    val members = memberService.availableMembers("joh").unsafeRunSync()

    members shouldBe MemberSearchResult(List(MemberSearchResultItem("John", "cn=John,dc=example,dc=io")), List.empty)
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
