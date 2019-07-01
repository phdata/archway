package com.heimdali.provisioning

import cats.Show

trait TypeWith[F[_]] {
  type Ev

  def value: Ev

  def evidence: F[Ev]

  implicit def show: Show[Ev]
}

object TypeWith {

  def apply[F[_], A](a: A)(implicit prov: F[A], s: Show[A]): TypeWith[F] = new TypeWith[F] {
    override type Ev = A

    override def value: A = a

    override def evidence: F[A] = prov

    override implicit def show: Show[A] = s
  }

}
