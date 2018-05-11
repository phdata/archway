package com.heimdali.repositories

import com.heimdali.models.Yarn

import scala.concurrent.Future

trait YarnRepository {
    def create(yarn: Yarn): Future[Yarn]
}
