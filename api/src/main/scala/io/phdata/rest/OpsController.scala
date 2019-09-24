package io.phdata.rest

import cats.effect._
import cats.implicits._
import com.typesafe.scalalogging.LazyLogging
import io.circe.syntax._
import io.phdata.models.{CustomLinkGroup, Full, Infra, User}
import io.phdata.rest.authentication.TokenAuthService
import io.phdata.services.{CustomLinkGroupService, WorkspaceService}
import org.http4s._
import org.http4s.dsl.Http4sDsl

class OpsController[F[_]: Sync](
    authService: TokenAuthService[F],
    workspaceService: WorkspaceService[F],
    customLinkGroupService: CustomLinkGroupService[F]
) extends Http4sDsl[F] with LazyLogging {

  val route: HttpRoutes[F] =
    authService.tokenRoleAuth(user => user.role == Infra || user.role == Full) {
      AuthedRoutes.of[User, F] {
        case GET -> Root / "workspaces" as _ =>
          for {
            result <- workspaceService.reviewerList(Infra).onError {
              case e: Throwable => logger.error(s"Failed to ops workspaces: ${e.getLocalizedMessage}", e).pure[F]
            }
            response <- Ok(result.asJson)
          } yield response

        case GET -> Root / "custom-links" as _ =>
          for {
            result <- customLinkGroupService.list.onError {
              case e: Throwable => logger.error(s"Failed to list custom links: ${e.getLocalizedMessage}", e).pure[F]
            }
            response <- Ok(result.asJson)
          } yield response

        case req @ POST -> Root / "custom-links" as _ =>
          implicit val decoder: EntityDecoder[F, CustomLinkGroup] = jsonOf[F, CustomLinkGroup]
          for {
            request <- req.req.as[CustomLinkGroup]
            _ <- customLinkGroupService.createCustomLinkGroup(request).onError {
              case e: Throwable =>
                logger.error(s"Failed to create custom link group: ${e.getLocalizedMessage}", e).pure[F]
            }
            response <- Created()
          } yield response

        case req @ PUT -> Root / "custom-links" / LongVar(id) as _ =>
          implicit val decoder: EntityDecoder[F, CustomLinkGroup] = jsonOf[F, CustomLinkGroup]
          for {
            request <- req.req.as[CustomLinkGroup]
            _ <- customLinkGroupService.updateCustomLinkGroup(id, request).onError {
              case e: Throwable =>
                logger
                  .error(s"Failed to update create custom link group ${request.id.get}: ${e.getLocalizedMessage}", e)
                  .pure[F]
            }
            response <- Ok()
          } yield response

        case DELETE -> Root / "custom-links" / LongVar(id) as _ =>
          for {
            _ <- customLinkGroupService.deleteCustomLinkGroup(id).onError {
              case e: Throwable =>
                logger.error(s"Failed to remove custom link group: ${e.getLocalizedMessage}", e).pure[F]
            }
            response <- Ok()
          } yield response

      }
    }

}
