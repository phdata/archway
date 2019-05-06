package com.heimdali.services

import java.io.{BufferedReader, File}
import java.nio.file.Paths

import cats.effect.{Resource, Sync}

import scala.collection.JavaConverters._
import scala.io.Source

class DefaultFileReader[F[_]](implicit val F: Sync[F]) extends FileReader[F] {

  override def reader(file: String): Resource[F, BufferedReader] =
    Resource.fromAutoCloseable(F.delay {
      Source.fromFile(new File(file)).bufferedReader()
    })

  override def readLines(file: String): F[List[String]] =
    reader(file).use(file => F.delay(file.lines().iterator().asScala.toList))

}
