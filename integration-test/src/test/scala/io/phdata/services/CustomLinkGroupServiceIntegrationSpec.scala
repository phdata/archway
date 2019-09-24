package io.phdata.services

import java.util.UUID

import cats.effect.{ContextShift, IO}
import cats.implicits._
import io.phdata.AppContext
import io.phdata.models.{CustomLink, CustomLinkGroup}
import io.phdata.test.fixtures.TestTimer
import org.scalatest.{BeforeAndAfterEach, FlatSpec}

import scala.concurrent.ExecutionContext

class CustomLinkGroupServiceIntegrationSpec extends FlatSpec with BeforeAndAfterEach{

  override def beforeEach(): Unit = clearCustomLinkGroups()
  override def afterEach(): Unit = clearCustomLinkGroups()

  behavior of "CustomLinkGroupService"

  it should "create a new custom link group" in new Context {
    val groupName = s"CustomLinkGroup-${UUID.randomUUID().toString.take(6)}"

    val result = resources.use { service =>
      for {
        _ <- service.createCustomLinkGroup(defaultCustomLinkGroup(groupName))
        dbGroups <- service.list
      } yield dbGroups
    }.unsafeRunSync()

    assert(result.size == 1)
    assert(result.get(0).get.name == groupName)
  }

  it should "update an existing custom link group" in new Context {
    val firstGroupName = s"CustomLinkGroup-${UUID.randomUUID().toString.take(6)}"
    val secondGroupName = s"CustomLinkGroup-${UUID.randomUUID().toString.take(6)}"

    val result = resources.use { service =>
      for {
        id <- service.createCustomLinkGroup(defaultCustomLinkGroup(firstGroupName))
        dbRecords <- service.list
        _ <- service.updateCustomLinkGroup (id, dbRecords.head.copy(name = secondGroupName))
        dbGroups <- service.list
      } yield dbGroups
    }.unsafeRunSync()

    assert(result.size == 1)
    assert(result.get(0).get.name == secondGroupName)
  }

  it should "return an empty list if no groups exist" in new Context {
    val result = resources.use { service =>
      for {
        dbGroups <- service.list
      } yield dbGroups
    }.unsafeRunSync()

    assert(result.isEmpty)
  }

  it should "delete a specific custom link group" in new Context {
    val firstGroupName = s"CustomLinkGroup-${UUID.randomUUID().toString.take(6)}"
    val secondGroupName = s"CustomLinkGroup-${UUID.randomUUID().toString.take(6)}"

    val result = resources.use { service =>
      for {
        firstGroupId <- service.createCustomLinkGroup(defaultCustomLinkGroup(firstGroupName))
        _ <- service.createCustomLinkGroup(defaultCustomLinkGroup(secondGroupName))
        _ <- service.deleteCustomLinkGroup(firstGroupId)
        dbGroups <- service.list
      } yield dbGroups
    }.unsafeRunSync()

    assert(result.size == 1)
    assert(result.get(0).get.name == secondGroupName)
  }

  private def clearCustomLinkGroups(): Unit = new Context {
    resources.use{
      service =>
        for{
          dbGroups <- service.list
          _ <- dbGroups.traverse(group => service.deleteCustomLinkGroup(group.id.get))
        } yield()
    }.unsafeRunSync()
  }

  private def defaultCustomLinkGroup(
      name: String,
      groupId: Option[Long] = None,
      firstLinkId: Option[Long] = None,
      secondLinkId: Option[Long] = None,
      thirdLinkId: Option[Long] = None
  ) = CustomLinkGroup(
    name,
    "Random test description",
    List(
      CustomLink("First custom link name", "First custom link random description", "http://localhost", groupId, firstLinkId),
      CustomLink("Second custom link name", "Second custom link random description", "http://localhost", groupId, secondLinkId),
      CustomLink("Third custom link name", "Third custom link random description", "http://localhost", groupId, thirdLinkId)
    ),
    groupId
  )

  trait Context{
    implicit def contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
    implicit def timer = new TestTimer

    val resources = for {
      ctx <- AppContext.default[IO]()
    } yield new CustomLinkGroupServiceImpl[IO](ctx)
  }

}
