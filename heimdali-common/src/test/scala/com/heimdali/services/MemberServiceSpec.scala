package com.heimdali.services

import cats.data.OptionT
import cats.effect.IO
import cats.implicits._
import com.heimdali.clients.{LDAPClient, LDAPUser}
import com.heimdali.models._
import com.heimdali.repositories._
import com.heimdali.test.fixtures._
import com.unboundid.ldap.sdk.{Attribute, SearchResultEntry}
import doobie._
import doobie.implicits._
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

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

  it should "add a member" in new Context {
    val newMember = s"cn=username,${appConfig.ldap.userPath.get}"
    ldapRepository.find _ expects("data", id, "manager") returning OptionT[ConnectionIO, LDAPRegistration](Option(savedLDAP).pure[ConnectionIO])
    memberRepository.create _ expects(newMember, id) returning id.pure[ConnectionIO]
    ldapClient.addUser _ expects(savedLDAP.distinguishedName, newMember) returning OptionT.some(newMember)
    memberRepository.complete _ expects(id, newMember) returning id.toInt.pure[ConnectionIO]
    memberRepository.get _ expects id returning List(MemberRightsRecord("data", newMember, savedHive.name, id, Manager)).pure[ConnectionIO]
    ldapClient.findUser _ expects newMember returning OptionT.some(LDAPUser(personName, "username", newMember, Seq.empty, Some("username@phdata.io")))

    memberService.addMember(123, MemberRoleRequest(newMember, "data", 123, Some(Manager))).value.unsafeRunSync()
  }

  trait Context {

    val memberRepository = mock[MemberRepository]
    val ldapRepository = mock[LDAPRepository]
    val ldapClient = mock[LDAPClient[IO]]
    val emailService = mock[EmailService[IO]]
    val workspaceRepository = mock[WorkspaceRequestRepository]

    val memberService =
      new MemberServiceImpl[IO](
        memberRepository,
        transactor,
        ldapRepository,
        ldapClient
      )

  }

}
