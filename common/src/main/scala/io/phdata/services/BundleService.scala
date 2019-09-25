package io.phdata.services

import scala.io.Source
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.{EntityDecoder, EntityEncoder, Header, Headers, HttpVersion, Method, Request, Uri}

import java.time.LocalDateTime
import java.net.InetAddress
import java.time.format.DateTimeFormatter

import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContext.Implicits.global
import cats.effect.{ConcurrentEffect, ExitCode, IO, IOApp}
import cats.implicits.catsSyntaxApplicativeId
import cats.implicits.catsSyntaxApply
import cats.implicits.catsSyntaxApplicativeError
import org.apache.http.entity.StringEntity

object BundleService extends IOApp with LazyLogging {
  override def run(args: List[String]): IO[ExitCode] = {
    val bundleService = new BundleService[IO]
    if (args(0).isEmpty) {
      logger.warn("Token is expected as the first argument to the application")
      sys.exit(1)
    } else {
      val bearerToken = args(0)
      val logFileName = "/var/log/archway/archway-server.log"
      val currentDate: String = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(LocalDateTime.now)
      val currentTime: String = DateTimeFormatter.ofPattern("HH:mm:ss").format(LocalDateTime.now)
      bundleService.postLog(bearerToken, logFileName, currentTime, currentDate).map(_ => ExitCode.Success)
    }
  }
}

class BundleService[F[_]: ConcurrentEffect]() extends LazyLogging {

  def postLog(bearerToken: String, logFileName: String, currentTime: String, currentDate: String): F[Unit] = {
    val logContents: String = Source.fromFile(logFileName).getLines().mkString
    val hostName: String = InetAddress.getLocalHost().getHostName()
    val requestUrl: String =
      s"https://repository.phdata.io/artifactory/support-private/archway/$currentDate/$currentTime.$hostName.${logFileName.split("/").last}"
    val postRequest = Request(
      Method.PUT,
      Uri.unsafeFromString(requestUrl),
      HttpVersion.`HTTP/1.0`,
      Headers.of(Header("Authorization", s"Bearer ${bearerToken}"))
    ).withEntity[String](logContents)(EntityEncoder[F, String])

    BlazeClientBuilder[F](global).resource.use { client =>
      logger.info("Start to send the log file to artifactory").pure[F] *> // ProductR
        client.expect(postRequest)(EntityDecoder.void).onError {
          case e: Throwable => logger.error(s"Artifactory request failed", e).pure[F]
        } *> // ProductL
        logger.info(s"Successfully sent to artifactory: $requestUrl").pure[F]
    }
  }
}
