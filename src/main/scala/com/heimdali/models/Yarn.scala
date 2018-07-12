package com.heimdali.models

import cats.Show
import cats.data.Kleisli
import cats.effect._
import cats.implicits._
import com.heimdali.tasks.ProvisionTask._
import com.heimdali.tasks.{ CreateResourcePool, ProvisionResult, ProvisionTask, Success, Error }
import io.circe._
import io.circe.generic.semiauto._
import io.circe.syntax._

case class Yarn(poolName: String,
                maxCores: Int,
                maxMemoryInGB: Int,
                id: Option[Long] = None)

object Yarn {

  implicit val viewer: Show[Yarn] =
    Show.show(y => s"creating resource pool ${y.poolName}")

  implicit def provisioner[F[_]](implicit F: Effect[F]): ProvisionTask[F, Yarn] = new ProvisionTask[F, Yarn] {
    override def provision(yarn: Yarn)(implicit F: Effect[F]): Kleisli[F, AppConfig[F], ProvisionResult] =
      CreateResourcePool(yarn.poolName, yarn.maxCores, yarn.maxMemoryInGB).provision
  }

  implicit val encoder: Encoder[Yarn] =
    Encoder.forProduct3("pool_name", "max_cores", "max_memory_in_gb")(s => (s.poolName, s.maxCores, s.maxMemoryInGB))

  implicit val decoder: Decoder[Yarn] =
    Decoder.forProduct3("pool_name", "max_cores", "max_memory_in_gb")((name: String, cores: Int, memory: Int) => Yarn(name, cores, memory))
}
