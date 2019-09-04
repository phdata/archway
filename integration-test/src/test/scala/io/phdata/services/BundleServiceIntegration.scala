package io.phdata.services

import cats.effect._
import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global
import org.scalatest.{FlatSpec, Matchers}
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.{EntityEncoder, Header, Headers, HttpVersion, Method, Request, Uri}
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.io._
import io.phdata.itest.fixtures._


class BundleServiceIntegration extends FlatSpec with Matchers{
  it should "send log file to artifactory" in new Context {
    val bearerToken = systemTestConfig.artifactoryToken
    val logFileName = "target/foo.log"
    val logContents: String = "bundle service test"
    val currentTime: String = DateTimeFormatter.ofPattern("HH:mm:ss").format(LocalDateTime.now)

    // Make new file foo.log
    new File("target").mkdirs()
    val pw = new PrintWriter(new File(logFileName))
    pw.write(logContents)
    pw.close()

    // Send foo.log to artifactory
    bundleService.postLog(bearerToken, logFileName, currentTime).unsafeRunSync()

    // Test if there is a foo.log in artifactory
    val getRequest = Request[IO](
      Method.GET,
      Uri.unsafeFromString(s"https://repository.phdata.io/artifactory/support-private/archway/${logFileName.split("/").last}+$currentTime"),
      HttpVersion.`HTTP/1.0`,
      Headers.of(Header("Authorization", s"Bearer ${bearerToken}"))
    )
    val getBuilder = httpClientBuilder.use(client =>
      IO{
        client.expect[String](getRequest).unsafeRunSync()
      }
    )
    assert(getBuilder.unsafeRunSync() == logContents)
  }

  trait Context{
    implicit val contextShift = IO.contextShift(ExecutionContext.global)
    implicit val concurrentEffect = IO.ioConcurrentEffect(contextShift)

    val bundleService = new BundleService[IO]
    val httpClientBuilder = BlazeClientBuilder[IO](global).resource
  }
}
