package com.heimdali.operations

import java.io.File

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import com.heimdali.clients.LDAPClientImpl
import com.heimdali.config.AppConfig
import com.typesafe.config.ConfigFactory

object Main extends IOApp {

  def ldapGet(configFile: String, dn: String): IO[ExitCode] =
    for {
      config <- io.circe.config.parser.decodePathF[IO, AppConfig](ConfigFactory.parseFile(new File(configFile)), "heimdali")
      client = new LDAPClientImpl[IO](config.ldap, _.provisioningBinding)
      result <- client.getEntry(dn).value
      _ <- result.foreach(r => println(s"raw result: ${r.toString()}")).pure[IO]
      _ <- result.map(client.ldapUser).foreach(r => println(s"converted result: $r")).pure[IO]
    } yield ExitCode.Success

  override def run(args: List[String]): IO[ExitCode] =
    args match {
      case "ldap-get" :: config :: dn :: Nil => ldapGet(config, dn)
    }
}