package com.heimdali.services

import cats.implicits._
import cats.effect.IO
import com.heimdali.clients.LDAPClient
import com.heimdali.repositories.{ LDAPRepository, MemberRepository }
import doobie.implicits._
import doobie._
import com.heimdali.models.WorkspaceMember
import com.heimdali.repositories.Manager
import org.scalamock.scalatest.MockFactory
import org.scalatest.{ FlatSpec, Matchers }
import com.heimdali.test.fixtures._

class MemberServiceSpec extends FlatSpec with Matchers with DBTest with MockFactory {

  behavior of "Member Service"

  it should "list members" in new Context {
    memberRepository.findByDatabase _ expects ("sesame", Manager) returning List(WorkspaceMember(standardUsername, None, None)).pure[ConnectionIO]

    val members =
      memberService.members(id, "sesame", Manager).unsafeRunSync()

    members shouldBe Seq(WorkspaceMember(standardUsername, None))
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
