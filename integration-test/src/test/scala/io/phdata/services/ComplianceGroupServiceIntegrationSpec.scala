package io.phdata.services

import java.util.UUID

import cats.effect.{ContextShift, IO}
import cats.implicits._
import io.phdata.AppContext
import io.phdata.models.{ComplianceGroup, ComplianceQuestion}
import io.phdata.test.fixtures.{TestTimer, testTimer}
import org.scalatest.{BeforeAndAfterEach, FlatSpec}

import scala.concurrent.ExecutionContext

class ComplianceGroupServiceIntegrationSpec extends FlatSpec with BeforeAndAfterEach{

  override def beforeEach(): Unit = clearComplianceGroups()
  override def afterEach(): Unit = clearComplianceGroups()

  behavior of "ComplianceGroupService"

  it should "create a new compliance group" in new Context {
    val groupName = s"ComplianceGroup-${UUID.randomUUID().toString.take(6)}"

    val result = resources.use { service =>
      for {
        _ <- service.createComplianceGroup(defaultComplianceGroup(groupName))
        dbGroups <- service.list()
      } yield dbGroups
    }.unsafeRunSync()

    assert(result.size == 1)
    assert(result.get(0).get.name == groupName)
  }

  it should "update an existing compliance group" in new Context {
    val firstGroupName = s"ComplianceGroup-${UUID.randomUUID().toString.take(6)}"
    val secondGroupName = s"ComplianceGroup-${UUID.randomUUID().toString.take(6)}"

    val result = resources.use { service =>
      for {
        id <- service.createComplianceGroup(defaultComplianceGroup(firstGroupName))
        _ <- service.updateComplianceGroup(id, defaultComplianceGroup(secondGroupName, id.some, 1L.some, 2L.some, 3L.some))
        dbGroups <- service.list()
      } yield dbGroups
    }.unsafeRunSync()

    assert(result.size == 1)
    assert(result.get(0).get.name == secondGroupName)
  }

  it should "return an empty list if no groups exist" in new Context {
    val result = resources.use { service =>
      for {
        dbGroups <- service.list()
      } yield dbGroups
    }.unsafeRunSync()

    assert(result.isEmpty)
  }

  it should "delete a specific compliance group" in new Context {
    val firstGroupName = s"ComplianceGroup-${UUID.randomUUID().toString.take(6)}"
    val secondGroupName = s"ComplianceGroup-${UUID.randomUUID().toString.take(6)}"

    val result = resources.use { service =>
      for {
        firstGroupId <- service.createComplianceGroup(defaultComplianceGroup(firstGroupName))
        _ <- service.createComplianceGroup(defaultComplianceGroup(secondGroupName))
        _ <- service.deleteComplianceGroup(firstGroupId)
        dbGroups <- service.list()
      } yield dbGroups
    }.unsafeRunSync()

    assert(result.size == 1)
    assert(result.get(0).get.name == secondGroupName)
  }

  private def clearComplianceGroups(): Unit = new Context {
    resources.use{
      service =>
        for{
        dbGroups <- service.list()
        _ <- dbGroups.traverse(group => service.deleteComplianceGroup(group.id.get))
        } yield()
    }.unsafeRunSync()
  }

  private def defaultComplianceGroup(
    name: String,
    groupId: Option[Long] = None,
    firstQuestionId: Option[Long] = None,
    secondQuestionId: Option[Long] = None,
    thirdQuestionId: Option[Long] = None,
  ) = ComplianceGroup(
    name,
    "Payment Card Industry [Data Security Standard",
    List(
      ComplianceQuestion("Full or partial credit card numbers?", "manager", testTimer.instant, groupId, firstQuestionId),
      ComplianceQuestion("Full or partial bank account numbers?", "manager", testTimer.instant, groupId, secondQuestionId),
      ComplianceQuestion("Any other combination of data that can be used to make purchases?", "manager", testTimer.instant, groupId, thirdQuestionId)
    ),
    groupId
  )

  trait Context{
    implicit def contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
    implicit def timer = new TestTimer

    val resources = for {
      ctx <- AppContext.default[IO]()
    } yield new ComplianceGroupServiceImpl[IO](ctx)
  }
}
