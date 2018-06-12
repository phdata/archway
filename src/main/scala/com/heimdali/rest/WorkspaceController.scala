package com.heimdali.rest

import java.time.Instant

import cats.effect._
import com.heimdali.models._
import com.heimdali.repositories.DatabaseRole
import com.heimdali.services._
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.{Decoder, Printer}
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.io._

class WorkspaceController(authService: AuthService[IO],
                          workspaceService: WorkspaceService[IO]) {

  implicit val memberRequestEntityDecoder: EntityDecoder[IO, MemberRequest] = jsonOf[IO, MemberRequest]

  val route: HttpService[IO] =
    authService.tokenAuth {
      AuthedService[User, IO] {
        case POST -> Root / LongVar(id) / "approval" as user =>
          if(user.canApprove)
            for {
              approved <- workspaceService.approve(id, Approval(user.role, user.username, Instant.now()))
              response <- Created(approved.asJson)
            } yield response
          else
            Forbidden()

        case req@POST -> Root as user =>
          /* explicit implicit declaration because of `user` variable */
          implicit val decoder: Decoder[WorkspaceRequest] = WorkspaceRequest.decoder(user)
          implicit val workspaceRequestEntityDecoder: EntityDecoder[IO, WorkspaceRequest] = jsonOf[IO, WorkspaceRequest]

          for {
            workspaceRequest <- req.req.as[WorkspaceRequest]
            newWorkspace <- workspaceService.create(workspaceRequest)
            response <- Created(newWorkspace.asJson)
          } yield response

        case GET -> Root as user =>
          for {
            workspaces <- workspaceService.list(user.username)
            response <- Ok(workspaces.asJson)
          } yield response

        case GET -> Root / LongVar(id) as _ =>
          for {
            maybeWorkspace <- workspaceService.find(id).value
            response <- maybeWorkspace.fold(NotFound())(workspace => Ok(workspace.asJson))
          } yield response

        case GET -> Root / LongVar(id) / database / DatabaseRole(role) as _ =>
          for {
            members <- workspaceService.members(id, database, role)
            response <- Ok(members.asJson)
          } yield response

        case req@POST -> Root / LongVar(id) / database / DatabaseRole(role) as _ =>
          for {
            memberRequest <- req.req.as[MemberRequest]
            newMember <- workspaceService.addMember(id, database, role, memberRequest.username).value
            response <- newMember.fold(NotFound())(member => Created(member.asJson))
          } yield response

        case DELETE -> Root / LongVar(id) / database / DatabaseRole(role) / username as _ =>
          for {
            removedMember <- workspaceService.removeMember(id, database, role, username).value
            response <- removedMember.fold(NotFound())(member => Ok(member.asJson))
          } yield response
      }
    }

}
