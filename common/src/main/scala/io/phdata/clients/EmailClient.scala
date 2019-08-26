package io.phdata.clients

import cats.effect.{Effect, IO}
import io.phdata.config.{AppConfig, SMTPConfig}
import com.typesafe.scalalogging.StrictLogging
import courier._

import scala.concurrent.{Await, ExecutionContext}
import scala.util.{Failure, Success}

trait EmailClient[F[_]] extends StrictLogging {

  def send(subject: String, content: String, from: String, to: String): F[Unit]

}

class EmailClientImpl[F[_]: Effect](appConfig: AppConfig, executionContext: ExecutionContext) extends EmailClient[F] {

  lazy val mailer: Mailer =
    appConfig.smtp match {
      case SMTPConfig(_, host, port, true, Some(user), Some(pass), ssl) =>
        Mailer(host, port).auth(true).as(user, pass.value).startTls(ssl)()

      case SMTPConfig(_, host, port, _, _, _, ssl) =>
        Mailer(host, port).startTls(ssl)()
    }

  override def send(subject: String, htmlContent: String, from: String, to: String): F[Unit] =
    Effect[F].liftIO(
      IO.fromFuture(
        IO.pure {
          val result =
            mailer(Envelope.from(from.addr).to(to.addr).subject(subject).content(Multipart().html(htmlContent)))(
              executionContext
            )

          result.onComplete {
            case Success(value) => logger.debug(s"Sent notification mail from ${from.addr} to ${to.addr}")
            case Failure(exception) =>
              logger.error(s"Failed to send email to ${to.addr} ${exception.getLocalizedMessage}", exception)
          }(executionContext)

          result
        }
      )
    )

}
