package com.heimdali.models

import cats.data.Kleisli
import cats.effect.Effect
import simulacrum._


@typeclass trait Savable[A] {

  @op("_V_") def save[F[_]](entity: A)(implicit F: Effect[F]): Kleisli[F, AppContext[F], Unit]

}
