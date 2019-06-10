package com.heimdali.services

import cats.data.OptionT
import cats.effect.IO
import cats.implicits._
import com.heimdali.AppContext
import com.heimdali.clients.LDAPUser
import com.heimdali.common.IntegrationTest
import com.heimdali.models._
import com.heimdali.repositories._
import com.heimdali.test.fixtures._
import doobie._
import doobie.implicits._
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

class MemberServiceIntegrationSpec
  extends FlatSpec
    with Matchers
    with DBTest
    with MockFactory
    with AppContextProvider
    with IntegrationTest {

  behavior of "Member Service"

  it should "list members" in new Context {
    val dataPermissions = MemberRightsRecord("data", standardUsername, systemName, id, Manager)
    context.memberRepository.list _ expects id returning List(dataPermissions).pure[ConnectionIO]
    context.lookupLDAPClient.findUser _ expects standardUsername returning OptionT.some(LDAPUser(personName, standardUsername, standardUserDN, Seq.empty, None))

    val members = memberService.members(id).unsafeRunSync()

    members shouldBe List(WorkspaceMemberEntry(standardUsername, personName, None, List(MemberRights(systemName, id, Manager)), List.empty, List.empty, List.empty))
  }

  it should "find members" in new Context {
    context.lookupLDAPClient.search _ expects "joh" returning MemberSearchResult(List(MemberSearchResultItem("John", "cn=John,dc=example,dc=io")), List.empty).pure[IO]

    memberService.availableMembers("joh").unsafeRunSync()
  }

  it should "add a member" in new Context {
    val newMember = s"cn=username,${appConfig.ldap.userPath.get}"
    context.ldapRepository.find _ expects("data", id, "manager") returning OptionT[ConnectionIO, LDAPRegistration](Option(savedLDAP).pure[ConnectionIO])
    context.memberRepository.create _ expects(newMember, id) returning id.pure[ConnectionIO]
    context.provisioningLDAPClient.addUser _ expects(savedLDAP.distinguishedName, newMember) returning OptionT.some(newMember)
    context.memberRepository.complete _ expects(id, newMember) returning id.toInt.pure[ConnectionIO]
    context.memberRepository.get _ expects id returning List(MemberRightsRecord("data", newMember, savedHive.name, id, Manager)).pure[ConnectionIO]
    context.lookupLDAPClient.findUser _ expects newMember returning OptionT.some(LDAPUser(personName, "username", newMember, Seq.empty, Some("username@phdata.io")))

    memberService.addMember(123, MemberRoleRequest(newMember, "data", 123, Some(Manager))).value.unsafeRunSync()
  }

  trait Context {

    val context: AppContext[IO] = genMockContext()

    val memberService = new MemberServiceImpl[IO](context)

  }

}
