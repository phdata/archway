package io.phdata.clients

import java.util.Base64

import cats.data.EitherT
import cats.effect.Sync
import org.pac4j.kerberos.credentials.authenticator.SunJaasKerberosTicketValidator
import org.springframework.core.io.FileSystemResource
import cats.implicits._
import io.phdata.config.AppConfig
import com.typesafe.scalalogging.LazyLogging

import scala.util.{Failure, Success, Try}

trait KerberosClient[F[_]] {
  def spnegoUsername(header: String): EitherT[F, Throwable, String]
}

class KerberosClientImpl[F[_]: Sync](appConfig: AppConfig) extends KerberosClient[F] with LazyLogging {

  def spnegoUsername(header: String): EitherT[F, Throwable, String] = {
    logger.debug("header " + header)
    // Header looks like: 'Negotiate <token>', strip off the token
    Try {
      val base64KerberosToken = header.substring(10)

      val kerberosToken = Base64.getDecoder.decode(base64KerberosToken)
      val validator = new SunJaasKerberosTicketValidator

      // validator.setDebug(true)
      validator.setServicePrincipal(appConfig.rest.httpPrincipal)
      validator.setKeyTabLocation(new FileSystemResource(appConfig.rest.keytab))

      val validationResult = validator.validateTicket(kerberosToken)
      validationResult.username().split("@").head
    } match {
      case Success(user)      => EitherT.right(user.pure[F])
      case Failure(exception) => EitherT.left(exception.pure[F])
    }
  }

}
