package com.heimdali.rest

import cats.effect._
import cats.implicits._
import com.heimdali.models._
import com.heimdali.services._
import io.circe.Decoder
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.dsl.Http4sDsl

class WorkspaceController[F[_] : Sync : Timer](authService: AuthService[F],
                                               workspaceService: WorkspaceService[F],
                                               memberService: MemberService[F],
                                               kafkaService: KafkaService[F],
                                               applicationService: ApplicationService[F],
                                               emailService: EmailService[F],
                                               provisioningService: ProvisioningService[F])
  extends Http4sDsl[F] {

  val clock = java.time.Clock.systemUTC()

  implicit val memberRequestEntityDecoder: EntityDecoder[F, MemberRequest] = jsonOf[F, MemberRequest]

  val route: HttpRoutes[F] =
    authService.tokenAuth {
      AuthedService[User, F] {
        case req@POST -> Root / LongVar(id) / "approve" as user =>
          if (user.canApprove) {
            implicit val decoder: Decoder[Approval] = Approval.decoder(user, clock)
            implicit val approvalEntityDecoder: EntityDecoder[F, Approval] = jsonOf[F, Approval]
            for {
              approval <- req.req.as[Approval]
              approved <- workspaceService.approve(id, approval)
              response <- Created(approved.asJson)
            } yield response
          }
          else
            Forbidden()

        case POST -> Root / LongVar(id) / "provision" as user =>
          if (user.isSuperUser) {
            for {
              workspace <- workspaceService.find(id).value
              provisionResult <- provisioningService.provision(workspace.get)
              response <- Created(provisionResult.asJson)
            } yield response
          }
          else
            Forbidden()

        case req@POST -> Root as user =>
          /* explicit implicit declaration because of `user` variable */
          implicit val decoder: Decoder[WorkspaceRequest] = WorkspaceRequest.decoder(user, clock)
          implicit val workspaceRequestEntityDecoder: EntityDecoder[F, WorkspaceRequest] = jsonOf[F, WorkspaceRequest]

          for {
            workspaceRequest <- req.req.as[WorkspaceRequest]
            newWorkspace <- workspaceService.create(workspaceRequest)
            _ <- emailService.newWorkspaceEmail(newWorkspace)
            response <- Created(newWorkspace.asJson)
          } yield response

        case GET -> Root as user =>
          for {
            workspaces <- workspaceService.list(user.distinguishedName)
            response <- Ok(workspaces.asJson)
          } yield response

        case GET -> Root / LongVar(id) as _ =>
          for {
            maybeWorkspace <- workspaceService.find(id).value
            response <- maybeWorkspace.fold(NotFound())(workspace => Ok(workspace.asJson))
          } yield response

        case GET -> Root / LongVar(id) / "members" as _ =>
          for {
            members <- memberService.members(id)
            response <- Ok(members.asJson)
          } yield response

        case req@POST -> Root / LongVar(id) / "members" as _ =>
          import MemberRoleRequest.decoder
          implicit val roleDecoder: EntityDecoder[F, MemberRoleRequest] = jsonOf[F, MemberRoleRequest]
          for {
            memberRequest <- req.req.as[MemberRoleRequest]
            newMember <- memberService.addMember(id, memberRequest).value
            _ <- emailService.newMemberEmail(id, memberRequest).value
            response <- newMember.fold(NotFound())(member => Created(member.asJson))
          } yield response

        case req@DELETE -> Root / LongVar(id) / "members" as _ =>
          import MemberRoleRequest.minDecoder
          implicit val roleDecoder: EntityDecoder[F, MemberRoleRequest] = jsonOf[F, MemberRoleRequest]
          for {
            memberRequest <- req.req.as[MemberRoleRequest]
            removedMember <- memberService.removeMember(id, memberRequest).value
            response <- Ok()
          } yield response

        case req@POST -> Root / LongVar(id) / "topics" as user =>
          implicit val kafkaTopicDecoderBase: Decoder[TopicRequest] = TopicRequest.decoder(user.username)
          implicit val kafkaTopicDecoder: EntityDecoder[F, TopicRequest] = jsonOf[F, TopicRequest]
          for {
            topic <- req.req.as[TopicRequest]
            result <- kafkaService.create(user.username, id, topic)
            response <- Ok(result.asJson)
          } yield response

        case req@POST -> Root / LongVar(id) / "applications" as user =>
          implicit val applicationDecoder: EntityDecoder[F, ApplicationRequest] = jsonOf[F, ApplicationRequest]
          for {
            request <- req.req.as[ApplicationRequest]
            result <- applicationService.create(user.username, id, request)
            response <- Ok(result.asJson)
          } yield response

        case GET -> Root / LongVar(id) / "yarn" as _ =>
          implicit val yarnInfoDecoder: EntityDecoder[F, List[YarnInfo]] = jsonOf[F, List[YarnInfo]]
          for {
            result <- workspaceService.yarnInfo(id)
            response <- Ok(result.asJson)
          } yield response

        case GET -> Root / LongVar(id) / "hive" as user =>
          implicit val hiveDecoder: EntityDecoder[F, HiveDatabase] = jsonOf[F, HiveDatabase]
          for {
            result <- workspaceService.hiveDetails(id)
            response <- Ok(result.asJson)
          } yield response

      }
    }

}
