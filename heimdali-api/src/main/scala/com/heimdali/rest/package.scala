package com.heimdali

import cats.effect.Sync
import io.circe.{Json, Printer}
import org.http4s.EntityDecoder
import org.http4s.circe.CirceInstances

package object rest /*extends CirceInstances*/ {
//  override val defaultPrinter: Printer =
//    Printer.noSpaces.copy(dropNullValues = true)
//
//  override implicit def jsonDecoder[F[_]](implicit evidence: Sync[F]): EntityDecoder[F, Json] =
//    CirceInstances.builder.build.jsonDecoder

}
