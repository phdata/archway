package io.phdata.clients

import cats.effect.{Effect, IO}
import com.typesafe.scalalogging.StrictLogging
import courier._
import io.phdata.config.{AppConfig, SMTPConfig}

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

trait EmailClient[F[_]] extends StrictLogging {

  def send(subject: String, content: Multipart, from: String, to: String): F[Unit]

}

class EmailClientImpl[F[_]: Effect](appConfig: AppConfig, executionContext: ExecutionContext) extends EmailClient[F] {

  lazy val mailer: Mailer =
    appConfig.smtp match {
      case SMTPConfig(_, host, port, true, Some(user), Some(pass), ssl, _) =>
        Mailer(host, port).auth(true).as(user, pass.value).startTls(ssl)()

      case SMTPConfig(_, host, port, _, _, _, ssl, smtps) =>
        Mailer(host, port).startTls(ssl).ssl(smtps)()
    }

  override def send(subject: String, htmlContent: Multipart, from: String, to: String): F[Unit] =
    Effect[F].liftIO(
      IO.fromFuture(
        IO.pure {
          val result =
            mailer(Envelope.from(from.addr).to(to.addr).subject(subject).content(htmlContent))(
              executionContext
            )

          result.onComplete {
            case Success(_) => logger.debug(s"Sent notification mail from ${from.addr} to ${to.addr}")
            case Failure(exception) =>
              logger.error(s"Failed to send email to ${to.addr} ${exception.getLocalizedMessage}", exception)
          }(executionContext)

          result
        }
      )
    )

}
