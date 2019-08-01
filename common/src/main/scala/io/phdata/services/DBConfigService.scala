package io.phdata.services

import atto.Atto._
import atto.ParseResult.Done
import cats.Monad
import io.phdata.AppContext
import doobie.implicits._

class DBConfigService[F[_]: Monad](context: AppContext[F]) extends ConfigService[F] {

  override def getAndSetNextGid: F[Long] =
    (for {
      value <- context.configRepository.getValue("nextgid")
      Done(_, currentgid) = long.parseOnly(value)
      nextgid = currentgid + 1
      _ <- context.configRepository.setValue("nextgid", nextgid.toString)
    } yield currentgid).transact(context.transactor)

  override def verifyDbConnection: F[Unit] =
    (for {
      _ <- context.configRepository.getValue("nextgid")
    } yield ()).transact(context.transactor)

}
