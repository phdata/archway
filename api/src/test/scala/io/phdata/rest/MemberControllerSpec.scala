package io.phdata.rest

import cats.effect.IO
import io.phdata.services.{MemberSearchResult, MemberSearchResultItem, MemberService}
import io.phdata.test.TestAuthService
import io.phdata.test.fixtures._
import io.phdata.services.{MemberSearchResult, MemberSearchResultItem, MemberService}
import io.phdata.test.TestAuthService
import io.phdata.test.fixtures.HttpTest
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.implicits._
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

class  MemberControllerSpec
  extends FlatSpec
    with Matchers
    with MockFactory
    with HttpTest {

  behavior of "Member controller"

  it should "return a list of users and groups" in new Http4sClientDsl[IO] with Context {
    (memberService.availableMembers _).expects("jo").returning(IO.pure(MemberSearchResult(
      List(MemberSearchResultItem("John Doe", "cn=John Doe,dc=example,dc=io")),
      List(MemberSearchResultItem("John's Group", "cn=johngroup,dc=example,dc=io"))
    )))

    val response = memberController.route.orNotFound.run(GET(Uri.uri("/jo")).unsafeRunSync())

    check(response, Status.Ok, Some(fromResource("rest/members.jo.json")))
  }

  trait Context {
    val authService: TestAuthService = new TestAuthService
    val memberService: MemberService[IO] = mock[MemberService[IO]]

    lazy val memberController: MemberController[IO] = new MemberController(authService, memberService)
  }

}
