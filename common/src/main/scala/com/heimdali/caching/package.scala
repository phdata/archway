package com.heimdali

import cats.effect.concurrent.MVar

package object caching {

  type CacheEntry[A] = (Long, A)

  type Cached[F[_], A] = MVar[F, CacheEntry[A]]

}
