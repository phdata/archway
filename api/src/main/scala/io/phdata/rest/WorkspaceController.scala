package io.phdata.rest

import java.time.Instant

import cats.effect._
import cats.implicits._
import com.typesafe.scalalogging.LazyLogging
import io.circe.Decoder
import io.circe.generic.auto._
import io.circe.syntax._
import io.phdata.AppContext
import io.phdata.models._
import io.phdata.provisioning.Message._
import io.phdata.provisioning.{Error, ExceptionMessage, NoOp, SimpleMessage, Success, Unknown}
import io.phdata.rest.authentication.TokenAuthService
import io.phdata.services._
import org.http4s._
import org.http4s.dsl.Http4sDsl

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext

class WorkspaceController[F[_]: Sync: Timer: ContextShift: ConcurrentEffect](
    appContext: AppContext[F],
    authService: TokenAuthService[F],
    workspaceService: WorkspaceService[F],
    memberService: MemberService[F],
    kafkaService: KafkaService[F],
    applicationService: ApplicationService[F],
    emailService: EmailService[F],
    provisioningService: ProvisioningService[F],
    yarnService: YarnService[F],
    hdfsService: HDFSService[F],
    complianceGroupService: ComplianceGroupService[F],
    emailEC: ExecutionContext,
    impalaService: ImpalaService
) extends Http4sDsl[F] with LazyLogging {

  implicit val memberRequestEntityDecoder: EntityDecoder[F, MemberRequest] = jsonOf[F, MemberRequest]

  // Assign case objects to values so we can match on them
  val success = Success
  val error = Error
  val noop = NoOp
  val unknown = Unknown

  val route: HttpRoutes[F] =
    authService.tokenAuth {
      AuthedRoutes.of[User, F] {
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
          if (user.isOpsUser) {
            for {
              workspace <- workspaceService.findById(id).value
              provisionFiber <- provisioningService.attemptProvision(workspace.get, 0)
              provisionResult <- provisionFiber.join.onError {
                case e: Throwable =>
                  logger.error(s"Failed to provision workspace id $id: ${e.getLocalizedMessage}", e).pure[F]
              }
              response <- provisionResult.head match {
                case _: SimpleMessage    => Created(provisionResult.asJson)
                case _: ExceptionMessage => InternalServerError(provisionResult.asJson)
              }
            } yield response
          } else
            Forbidden()

        case POST -> Root / LongVar(id) / "deprovision" as user =>
          if (user.isOpsUser) {
            for {
              workspace <- workspaceService.findById(id).value
              provisionFiber <- provisioningService.attemptDeprovision(workspace.get)
              provisionResult <- provisionFiber.join.onError {
                case e: Throwable =>
                  logger.error(s"Failed to deprovision workspace id $id: ${e.getLocalizedMessage}", e).pure[F]
              }
              response <- provisionResult.head match {
                case _: SimpleMessage    => Ok(provisionResult.asJson)
                case _: ExceptionMessage => InternalServerError(provisionResult.asJson)
              }
            } yield response
          } else
            Forbidden()

        case DELETE -> Root / LongVar(id) as user =>
          if (user.isOpsUser) {
            for {
              _ <- workspaceService.deleteWorkspace(id).onError {
                case e: Throwable =>
                  logger.error(s"Failed to delete workspace id $id: ${e.getLocalizedMessage}", e).pure[F]
              }
              response <- Ok()
            } yield response
          } else
            Forbidden()

        case POST -> Root / LongVar(id) / "owner" / ownerDN as user =>
          if (user.isOpsUser) {
            for {
              _ <- logger.info(s"Changing workspace owner for workspace '$id' to owner $ownerDN").pure[F]
              _ <- workspaceService.changeOwner(id, DistinguishedName(ownerDN)).onError {
                case e: Throwable =>
                  logger
                    .error(s"Failed to reassign workspace id $id to user $ownerDN: ${e.getLocalizedMessage}", e)
                    .pure[F]
              }
              response <- Ok()
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
              _ <- ConcurrentEffect[F]
                .start(ContextShift[F].evalOn(emailEC)(emailService.newWorkspaceEmail(newWorkspace)))
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

        case GET -> Root / LongVar(id) as user => {
          val access =
            for {
              userHasAccess <- workspaceService.userAccessible(user.distinguishedName, id)
              wsAccess <- (userHasAccess || user.canApprove).pure[F]
            } yield wsAccess

          val maybeWorkspaceT = access
            .flatMap(
              x =>
                if (x) {
                  workspaceService.findById(id).value
                } else {
                  None.asInstanceOf[Option[WorkspaceRequest]].pure[F]
                }
            )
            .onError {
              case e: Throwable =>
                logger
                  .error(s"Failed get workspace $id for user ${user.distinguishedName}: ${e.getLocalizedMessage}", e)
                  .pure[F]
            }

          maybeWorkspaceT.flatMap(_.fold(Forbidden())(workspace => Ok(workspace.asJson)))
        }

        case GET -> Root / LongVar(id) / "members" as _ =>
          for {
            members <- memberService.members(id).onError {
              case e: Throwable =>
                logger.error(s"Failed to get members for workspace $id: ${e.getLocalizedMessage}", e).pure[F]
            }
            response <- Ok(members.asJson)
          } yield response

        case req @ POST -> Root / LongVar(workspaceId) / "members" as user =>
          import MemberRoleRequest.decoder
          implicit val roleDecoder: EntityDecoder[F, MemberRoleRequest] = jsonOf[F, MemberRoleRequest]
          for {
            memberRequest <- req.req.as[MemberRoleRequest]
            _ <- logger
              .info(
                s"${user.name} is requesting to add a new member ${memberRequest.distinguishedName} in workspace ${workspaceId}"
              )
              .pure[F]
            newMember <- memberService.addMember(workspaceId, memberRequest).value.onError {
              case e: Throwable =>
                logger.error(s"Failed to add member to workspace $workspaceId: ${e.getLocalizedMessage}", e).pure[F]
            }
            _ <- impalaService.invalidateMetadata(workspaceId)(appContext).onError {
              case e: Throwable =>
                logger
                  .error(
                    s"Failed to invalidate impala metadata for workspace $workspaceId: ${e.getLocalizedMessage}",
                    e
                  )
                  .pure[F]
            }
            _ <- ConcurrentEffect[F]
              .start(ContextShift[F].evalOn(emailEC)(emailService.newMemberEmail(workspaceId, memberRequest).value))
            response <- newMember.fold(InternalServerError())(member => Created(member.asJson))
          } yield response

        case req @ POST -> Root / LongVar(workspaceId) / "members" / "batch" as user =>
          import MemberRoleRequest.decoder
          implicit val roleDecoder: EntityDecoder[F, List[MemberRoleRequest]] = jsonOf[F, List[MemberRoleRequest]]

          val errors = new ListBuffer[MemberRoleRequest]()

          for {
            memberRequests <- req.req.as[List[MemberRoleRequest]]

            _ <- logger
              .info(
                s"${user.name} is requesting to add a new members: ${memberRequests.map(r => r.distinguishedName.value)} in workspace ${workspaceId}"
              )
              .pure[F]

            _ <- memberRequests.traverse { request =>
              for {
                newMember <- memberService.addMember(workspaceId, request).value.onError {
                  case e: Throwable =>
                    errors += request
                    logger.error(s"Failed to add member to workspace $workspaceId: ${e.getLocalizedMessage}", e).pure[F]
                }
                _ <- ConcurrentEffect[F]
                  .start(ContextShift[F].evalOn(emailEC)(emailService.newMemberEmail(workspaceId, request).value))
              } yield (newMember)
            }
            _ <- impalaService.invalidateMetadata(workspaceId)(appContext).onError {
              case e: Throwable =>
                logger
                  .error(
                    s"Failed to invalidate impala metadata for workspace $workspaceId: ${e.getLocalizedMessage}",
                    e
                  )
                  .pure[F]
            }
            response <- if (errors.isEmpty) Created() else InternalServerError()
          } yield response

        case req @ DELETE -> Root / LongVar(id) / "members" as user =>
          import MemberRoleRequest.minDecoder
          implicit val roleDecoder: EntityDecoder[F, MemberRoleRequest] = jsonOf[F, MemberRoleRequest]
          for {
            memberRequest <- req.req.as[MemberRoleRequest]
            _ <- logger
              .info(
                s"${user.name} is requesting to remove a member ${memberRequest.distinguishedName} in workspace ${id}"
              )
              .pure[F]
            _ <- memberService.removeMember(id, memberRequest).value.onError {
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
            result <- kafkaService.create(user.distinguishedName, id, topic).onError {
              case e: Throwable =>
                logger.error(s"Failed to create topic for workspace $id: ${e.getLocalizedMessage}", e).pure[F]
            }
            response <- Ok(result.asJson)
          } yield response

        case req @ POST -> Root / LongVar(id) / "applications" as user =>
          implicit val applicationDecoder: EntityDecoder[F, ApplicationRequest] = jsonOf[F, ApplicationRequest]
          for {
            request <- req.req.as[ApplicationRequest]
            result <- applicationService.create(user.distinguishedName, id, request).onError {
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

        case GET -> Root / LongVar(id) / "status" as _ =>
          for {
            workspaceStatus <- workspaceService.status(id).onError {
              case e: Throwable =>
                logger.error(s"Failed to get status for workspace $id: ${e.getLocalizedMessage}", e).pure[F]
            }
            response <- Ok(workspaceStatus.asJson)
          } yield response

        case GET -> Root / "questions" as user =>
          if (user.isRiskUser) {
            for {
              groups <- complianceGroupService.list.onError {
                case e: Throwable =>
                  logger.error(s"Failed to get list of compliance groups: ${e.getLocalizedMessage}", e).pure[F]
              }
              response <- Ok(groups.asJson)
            } yield response
          } else
            Forbidden()

        case req @ POST -> Root / "questions" as user =>
          if (user.isRiskUser) {
            Clock[F].realTime(scala.concurrent.duration.MILLISECONDS).flatMap { time =>
              implicit val decoder: Decoder[ComplianceGroup] =
                ComplianceGroup.decoder(Instant.ofEpochMilli(time))
              implicit val complianceGroupDecoder: EntityDecoder[F, ComplianceGroup] =
                jsonOf[F, ComplianceGroup]

              for {
                request <- req.req.as[ComplianceGroup]
                _ <- complianceGroupService.createComplianceGroup(request).onError {
                  case e: Throwable =>
                    logger.error(s"Failed to create compliance group: ${e.getLocalizedMessage}", e).pure[F]
                }
                response <- Created()
              } yield response
            }
          } else
            Forbidden()

        case req @ PUT -> Root / "questions" / LongVar(id) as user =>
          if (user.isRiskUser) {
            Clock[F].realTime(scala.concurrent.duration.MILLISECONDS).flatMap { time =>
              implicit val decoder: Decoder[ComplianceGroup] =
                ComplianceGroup.decoder(Instant.ofEpochMilli(time))
              implicit val complianceGroupDecoder: EntityDecoder[F, ComplianceGroup] =
                jsonOf[F, ComplianceGroup]

              for {
                request <- req.req.as[ComplianceGroup]
                _ <- complianceGroupService.updateComplianceGroup(id, request).onError {
                  case e: Throwable =>
                    logger
                      .error(s"Failed to update compliance group ${request.id.get}: ${e.getLocalizedMessage}", e)
                      .pure[F]
                }
                response <- Ok()
              } yield response
            }
          } else
            Forbidden()

        case DELETE -> Root / "questions" / LongVar(id) as user =>
          if (user.isRiskUser) {
            for {
              _ <- complianceGroupService.deleteComplianceGroup(id).onError {
                case e: Throwable =>
                  logger.error(s"Failed to remove compliance group: ${e.getLocalizedMessage}", e).pure[F]
              }
              response <- Ok()
            } yield response
          } else
            Forbidden()

        case req @ POST -> Root / LongVar(id) / "yarn" as user =>
          if (user.isOpsUser) {
            implicit val yarnDecoder: EntityDecoder[F, Yarn] = jsonOf[F, Yarn]
            for {
              request <- req.req.as[Yarn]
              currentYarn <- yarnService.list(id)
              _ <- yarnService.updateYarnResources(request, currentYarn.head.id.get, Instant.now()).onError {
                case e: Throwable =>
                  logger
                    .error(
                      s"Failed to update the number of cores and memories in ${request.poolName}: ${e.getLocalizedMessage()}",
                      e
                    )
                    .pure[F]
              }
              response <- Ok()
            } yield response
          } else
            Forbidden()

        case POST -> Root / LongVar(id) / "disk-quota" / LongVar(resourceId) / IntVar(size) as user =>
          if (user.isOpsUser) {
            for {
              workspace <- workspaceService.findById(id).value
              hiveAllocation <- workspace.get.data.find(_.id.get == resourceId).get.pure[F]
              _ <- if (hiveAllocation.getProtocol == "hdfs") {
                hdfsService
                  .setQuota(
                    hiveAllocation.location,
                    size,
                    resourceId,
                    Instant.now()
                  )
                  .onError {
                    case e: Throwable =>
                      logger.error(s"Failed to modify disk-quota of workspace $id: ${e.getLocalizedMessage}", e).pure[F]
                  }
              } else {
                ().pure[F]
              }
              response <- Ok()
            } yield response
          } else
            Forbidden()
      }
    }

}
