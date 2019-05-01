package com.heimdali.services

import atto._
import Atto._
import atto.ParseResult.Done
import cats.Monad
import com.heimdali.config.AppConfig
import com.heimdali.repositories.ConfigRepository
import doobie._
import doobie.implicits._

class DBConfigService[F[_] : Monad](appConfig: AppConfig,
                                    configRepository: ConfigRepository,
                                    transactor: Transactor[F])
  extends ConfigService[F] {

  override def getAndSetNextGid: F[Long] =
    (for {
      value <- configRepository.getValue("nextgid")
      Done(_, currentgid) = long.parseOnly(value)
      nextgid = currentgid + 1
      _ <- configRepository.setValue("nextgid", nextgid.toString)
    } yield currentgid).transact(transactor)

  override def getTemplate(templateName: String): F[String] = ???
}
