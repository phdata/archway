package com.heimdali.actors

import com.heimdali.services.HiveService

import scala.concurrent.Future

class HappyHiveService extends HiveService {
  import scala.concurrent.ExecutionContext.Implicits.global

  override def createRole(name: String): Future[Option[Int]] =
    Future(Some(1))

  override def createDatabase(name: String, location: String): Future[Option[Int]] =
    Future(Some(1))

  override def grantGroup(group: String, role: String): Future[Int] =
    Future(1)

  override def enableAccessToDB(database: String, role: String): Future[Int] =
    Future(1)

  override def enableAccessToLocation(location: String, role: String): Future[Int] =
    Future(1)
}
