package com.heimdali

import cats.effect.Sync
import io.circe.{Json, Printer}
import org.http4s.EntityDecoder
import org.http4s.circe.CirceInstances

package object rest extends CirceInstances {
  override val defaultPrinter: Printer =
    Printer.noSpaces.copy(dropNullValues = true)

  override def jsonDecoder[F[_]: Sync]: EntityDecoder[F, Json] =
    CirceInstances.defaultJsonDecoder
}