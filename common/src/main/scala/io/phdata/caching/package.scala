package io.phdata

import cats.effect.concurrent.MVar

package object caching {

  case class CacheEntry[A](cachedTime: Long, entry: A)

  type Cached[F[_], A] = MVar[F, CacheEntry[A]]

}
