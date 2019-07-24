package com.heimdali.rest

import java.time.Instant

import cats.effect._
import cats.implicits._
import com.heimdali.models._
import com.heimdali.provisioning.Message._
import com.heimdali.rest.authentication.TokenAuthService
import com.heimdali.services._
import com.typesafe.scalalogging.LazyLogging
import io.circe.Decoder
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.dsl.Http4sDsl

import scala.concurrent.ExecutionContext

class WorkspaceController[F[_]: Sync: Timer: ContextShift: ConcurrentEffect](
    authService: TokenAuthService[F],
    workspaceService: WorkspaceService[F],
    memberService: MemberService[F],
    kafkaService: KafkaService[F],
    applicationService: ApplicationService[F],
    emailService: EmailService[F],
    provisioningService: ProvisioningService[F],
    emailEC: ExecutionContext
) extends Http4sDsl[F] with LazyLogging {

  implicit val memberRequestEntityDecoder: EntityDecoder[F, MemberRequest] = jsonOf[F, MemberRequest]

  val route: HttpRoutes[F] =
    authService.tokenAuth {
      AuthedService[User, F] {
        case req @ POST -> Root / LongVar(id) / "approve" as user =>
          if (user.canApprove) {
            Clock[F].realTime(scala.concurrent.duration.MILLISECONDS).flatMap { time =>
              implicit val decoder: Decoder[Approval] = Approval.decoder(user, Instant.ofEpochMilli(time))
              implicit val approvalEntityDecoder: EntityDecoder[F, Approval] = jsonOf[F, Approval]
              for {
                approval <- req.req.as[Approval]
                approved <- workspaceService.approve(id, approval).onError {
                  case e: Throwable =>
                    logger.error(s"Failed to approve workspace $id: ${e.getLocalizedMessage}", e).pure[F]
                }
                response <- Created(approved.asJson)
              } yield response
            }
          } else
            Forbidden()

        case POST -> Root / LongVar(id) / "provision" as user =>
          if (user.isSuperUser) {
            for {
              workspace <- workspaceService.find(id).value
              provisionFiber <- provisioningService.attemptProvision(workspace.get, 0)
              provisionResult <- provisionFiber.join.onError {
                case e: Throwable =>
                  logger.error(s"Failed to provision workspace id $id: ${e.getLocalizedMessage}", e).pure[F]
              }
              response <- Created(provisionResult.asJson)
            } yield response
          } else
            Forbidden()

        case POST -> Root / LongVar(id) / "deprovision" as user =>
          if (user.isSuperUser) {
            for {
              workspace <- workspaceService.find(id).value
              provisionFiber <- provisioningService.attemptDeprovision(workspace.get)
              provisionResult <- provisionFiber.join.onError {
                case e: Throwable =>
                  logger.error(s"Failed to deprovision workspace id $id: ${e.getLocalizedMessage}", e).pure[F]
              }
              response <- Created(provisionResult.asJson)
            } yield response
          } else
            Forbidden()

        case req @ POST -> Root as user =>
          /* explicit implicit declaration because of `user` variable */
          Clock[F].realTime(scala.concurrent.duration.MILLISECONDS).flatMap { time =>
            implicit val decoder: Decoder[WorkspaceRequest] =
              WorkspaceRequest.decoder(user.distinguishedName, Instant.ofEpochMilli(time))
            implicit val workspaceRequestEntityDecoder: EntityDecoder[F, WorkspaceRequest] = jsonOf[F, WorkspaceRequest]
            for {
              workspaceRequest <- req.req.as[WorkspaceRequest]
              newWorkspace <- workspaceService.create(workspaceRequest).onError {
                case e: Throwable =>
                  logger
                    .error(s"Failed to create workspace for id ${workspaceRequest.id}: ${e.getLocalizedMessage}", e)
                    .pure[F]
              }
              _ <- ConcurrentEffect[F].start(
                ContextShift[F].evalOn(emailEC)(emailService.newWorkspaceEmail(newWorkspace)))
              response <- Created(newWorkspace.asJson)
            } yield response
          }

        case GET -> Root as user =>
          for {
            workspaces <- workspaceService.list(user.distinguishedName).onError {
              case e: Throwable => logger.error(s"Failed to list workspaces: ${e.getLocalizedMessage}", e).pure[F]
            }
            response <- Ok(workspaces.asJson)
          } yield response

        case GET -> Root / LongVar(id) as _ =>
          for {
            maybeWorkspace <- workspaceService.find(id).value.onError {
              case e: Throwable => logger.error(s"Failed to find workspace $id: ${e.getLocalizedMessage}", e).pure[F]
            }
            response <- maybeWorkspace.fold(NotFound())(workspace => Ok(workspace.asJson))
          } yield response

        case GET -> Root / LongVar(id) / "members" as _ =>
          for {
            members <- memberService.members(id).onError {
              case e: Throwable =>
                logger.error(s"Failed to get members for workspace $id: ${e.getLocalizedMessage}", e).pure[F]
            }
            response <- Ok(members.asJson)
          } yield response

        case req @ POST -> Root / LongVar(id) / "members" as _ =>
          import MemberRoleRequest.decoder
          implicit val roleDecoder: EntityDecoder[F, MemberRoleRequest] = jsonOf[F, MemberRoleRequest]
          for {
            memberRequest <- req.req.as[MemberRoleRequest]
            newMember <- memberService.addMember(id, memberRequest).value.onError {
              case e: Throwable =>
                logger.error(s"Failed to add member to workspace $id: ${e.getLocalizedMessage}", e).pure[F]
            }
            _ <- emailService.newMemberEmail(id, memberRequest).value
            response <- newMember.fold(InternalServerError())(member => Created(member.asJson))
          } yield response

        case req @ DELETE -> Root / LongVar(id) / "members" as _ =>
          import MemberRoleRequest.minDecoder
          implicit val roleDecoder: EntityDecoder[F, MemberRoleRequest] = jsonOf[F, MemberRoleRequest]
          for {
            memberRequest <- req.req.as[MemberRoleRequest]
            removedMember <- memberService.removeMember(id, memberRequest).value.onError {
              case e: Throwable =>
                logger.error(s"Failed to remove member from workspace $id: ${e.getLocalizedMessage}", e).pure[F]
            }
            response <- Ok()
          } yield response

        case req @ POST -> Root / LongVar(id) / "topics" as user =>
          implicit val kafkaTopicDecoderBase: Decoder[TopicRequest] = TopicRequest.decoder(user.username)
          implicit val kafkaTopicDecoder: EntityDecoder[F, TopicRequest] = jsonOf[F, TopicRequest]
          for {
            topic <- req.req.as[TopicRequest]
            result <- kafkaService.create(user.username, id, topic).onError {
              case e: Throwable =>
                logger.error(s"Failed to create topic for workspace $id: ${e.getLocalizedMessage}", e).pure[F]
            }
            response <- Ok(result.asJson)
          } yield response

        case req @ POST -> Root / LongVar(id) / "applications" as user =>
          implicit val applicationDecoder: EntityDecoder[F, ApplicationRequest] = jsonOf[F, ApplicationRequest]
          for {
            request <- req.req.as[ApplicationRequest]
            result <- applicationService.create(user.username, id, request).onError {
              case e: Throwable =>
                logger.error(s"Failed to get applications for workspace $id: ${e.getLocalizedMessage}", e).pure[F]
            }
            response <- Ok(result.asJson)
          } yield response

        case GET -> Root / LongVar(id) / "yarn" as _ =>
          implicit val yarnInfoDecoder: EntityDecoder[F, List[YarnInfo]] = jsonOf[F, List[YarnInfo]]
          for {
            result <- workspaceService.yarnInfo(id).onError {
              case e: Throwable =>
                logger.error(s"Failed to get yarn info for workspace $id: ${e.getLocalizedMessage}", e).pure[F]
            }
            response <- Ok(result.asJson)
          } yield response

        case GET -> Root / LongVar(id) / "hive" as user =>
          implicit val hiveDecoder: EntityDecoder[F, HiveDatabase] = jsonOf[F, HiveDatabase]
          for {
            result <- workspaceService.hiveDetails(id).onError {
              case e: Throwable =>
                logger.error(s"Failed get hive details for workspace $id: ${e.getLocalizedMessage}", e).pure[F]
            }
            response <- Ok(result.asJson)
          } yield response
          
      }
    }

}
