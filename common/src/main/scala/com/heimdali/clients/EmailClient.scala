package com.heimdali.clients

import cats.effect.{Effect, IO}
import com.heimdali.config.{AppConfig, SMTPConfig}
import courier._

import scala.concurrent.ExecutionContext

trait EmailClient[F[_]] {

  def send(subject: String, content: String, from: String, to: String): F[Unit]

}

class EmailClientImpl[F[_] : Effect](appConfig: AppConfig,
                                     executionContext: ExecutionContext)
  extends EmailClient[F] {

  lazy val mailer: Mailer =
    appConfig.smtp match {
    case SMTPConfig(_, host, port, true, Some(user), Some(pass), ssl) =>
      Mailer(host, port)
        .auth(true)
        .as(user, pass)
        .startTls(ssl)()

    case SMTPConfig(_, host, port, _, _, _, ssl) =>
      Mailer(host, port)
        .startTls(ssl)()
  }

  override def send(subject: String, htmlContent: String, from: String, to: String): F[Unit] =
    Effect[F].liftIO(IO.fromFuture(IO.pure(
      mailer(
        Envelope
          .from(from.addr)
          .to(to.addr)
          .subject(subject)
          .content(Multipart().html(htmlContent))
      )(executionContext)
    )))

}
