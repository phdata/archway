package com.heimdali.clients

import cats.effect._
import courier._
import javax.mail.Message
import javax.mail.internet.MimeMultipart
import org.jvnet.mock_javamail.Mailbox
import org.scalatest.{FlatSpec, Matchers}

class EmailClientImplSpec extends FlatSpec with Matchers {

  behavior of "EmailClientImplSpec"

  ignore should "send" in {
    import scala.concurrent.ExecutionContext.Implicits.global
    val mailer = Mailer("localhost", 25)()
    val emailClient = new EmailClientImpl[IO](mailer)
    val subject: String = "Hey There"
    val body: String = "Welcome home!"

    emailClient.send(subject, body, "me@me.com", "you@you.com").unsafeRunSync()

    val inbox = Mailbox.get("you@you.com")
    val message: Message = inbox.get(0)

    message.getSubject shouldBe subject
    message.getContent.asInstanceOf[MimeMultipart].getBodyPart(0).getContent shouldBe s"<p>$body</p>"
  }

}