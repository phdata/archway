package com.heimdali.clients

import cats.effect.{Effect, IO}
import courier._
import javax.mail.internet
import javax.mail.internet.InternetAddress

import scala.concurrent.ExecutionContext

trait EmailClient[F[_]] {

  def send(subject: String, content: scalatags.Text.TypedTag[String], from: String, to: String): F[Unit]

}

class EmailClientImpl[F[_]](mailer: Mailer)
                           (implicit val F: Effect[F], executionContext: ExecutionContext)
  extends EmailClient[F] {

  override def send(subject: String, content: scalatags.Text.TypedTag[String], from: String, to: String): F[Unit] =
    F.liftIO(IO.fromFuture(IO.pure(
      mailer(
        Envelope
          .from(from.addr)
          .to(to.addr)
          .subject(subject)
          .content(
            Multipart()
              .html(content.render)
          )
      )
    )))

}
