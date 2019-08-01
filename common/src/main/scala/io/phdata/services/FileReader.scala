package io.phdata.services

import java.io.BufferedReader

import cats.effect.Resource

trait FileReader[F[_]] {

  def reader(file: String): Resource[F, BufferedReader]

  def readLines(file: String): F[List[String]]

}
