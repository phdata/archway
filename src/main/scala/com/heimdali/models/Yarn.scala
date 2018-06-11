package com.heimdali.models

import io.circe._
import io.circe.generic.semiauto._
import io.circe.syntax._

case class Yarn(poolName: String,
                maxCores: Int,
                maxMemoryInGB: Int,
                id: Option[Long] = None)

object Yarn {
  implicit val encoder: Encoder[Yarn] =
    Encoder.forProduct3("pool_name", "max_cores", "max_memory_in_gb")(s => (s.poolName, s.maxCores, s.maxMemoryInGB))

  implicit val decoder: Decoder[Yarn] =
    Decoder.forProduct3("pool_name", "max_cores", "max_memory_in_gb")((name: String, cores: Int, memory: Int) => Yarn(name, cores, memory))
}