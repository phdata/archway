package io.phdata.services

import cats.effect.{Clock, ContextShift, IO, Timer}
import cats.implicits._
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import io.phdata.AppContext
import io.phdata.models.CustomLink
import io.phdata.test.fixtures.{AppContextProvider, DBTest, _}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.ExecutionContext

class CustomLinkGroupServiceSpec  extends FlatSpec with Matchers with DBTest with MockFactory with AppContextProvider {

  override implicit val contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  behavior of "CustomLinkGroupService"

  it should "list custom link groups" in new Context {
    context.customLinkGroupRepository.list _ expects () returning List(customLinkGroup()).pure[ConnectionIO]

    val groups = customLinkGroupService.list.unsafeRunSync()
    groups shouldBe List(customLinkGroup())
  }

  it should "create a new custom link group" in new Context {
    context.customLinkGroupRepository.create _ expects customLinkGroup() returning 1L.pure[ConnectionIO]
    context.customLinkRepository.create _ expects (1, customLinkGroup().links.get(0).get) returning 1L.pure[ConnectionIO]
    context.customLinkRepository.create _ expects (1, customLinkGroup().links.get(1).get) returning 2L.pure[ConnectionIO]

    val creationResult = customLinkGroupService.createCustomLinkGroup(customLinkGroup()).unsafeRunSync()
    creationResult shouldBe 1L
  }

  it should "update an existing custom link group - change name" in new Context {
    val savedCustomLinkGroup = customLinkGroup(1L.some, 1L.some, 2L.some)
    val updatedCustomLinkGroup = savedCustomLinkGroup.copy(name = "Updated group name")

    context.customLinkRepository.findByCustomLinkGroupId _ expects 1L returning savedCustomLinkGroup.links.pure[ConnectionIO]
    context.customLinkRepository.update _ expects savedCustomLinkGroup.links.get(0).get returning 1.pure[ConnectionIO]
    context.customLinkRepository.update _ expects savedCustomLinkGroup.links.get(1).get returning 1.pure[ConnectionIO]
    context.customLinkGroupRepository.update _ expects (1L, updatedCustomLinkGroup) returning 1.pure[ConnectionIO]

    val updateResult =
      customLinkGroupService.updateCustomLinkGroup(1L, updatedCustomLinkGroup).unsafeRunSync()

    updateResult shouldBe ()
  }

  it should "update an existing custom link group - remove one of links" in new Context {
    val savedCustomLinkGroup = customLinkGroup(1L.some, 1L.some, 2L.some)
    val updatedCustomLinkGroup = savedCustomLinkGroup.copy(links = List(savedCustomLinkGroup.links.head))

    context.customLinkRepository.findByCustomLinkGroupId _ expects 1L returning savedCustomLinkGroup.links.pure[ConnectionIO]
    context.customLinkRepository.update _ expects savedCustomLinkGroup.links.get(0).get returning 1.pure[ConnectionIO]
    context.customLinkRepository.delete _ expects 2L returning ().pure[ConnectionIO]
    context.customLinkGroupRepository.update _ expects (1L, updatedCustomLinkGroup) returning 1.pure[ConnectionIO]

    val updateResult =
      customLinkGroupService.updateCustomLinkGroup(1L, updatedCustomLinkGroup).unsafeRunSync()

    updateResult shouldBe ()
  }

  it should "update an existing custom link group - add a new link" in new Context {
    val savedCustomLinkGroup = customLinkGroup(1L.some, 1L.some, 2L.some)
    val newLink = CustomLink("Third link", "Third custom link", "http://localhost", 1L.some)
    val updatedCustomLinkGroup = savedCustomLinkGroup.copy(links = savedCustomLinkGroup.links ++ List(newLink))

    context.customLinkRepository.findByCustomLinkGroupId _ expects 1L returning savedCustomLinkGroup.links.pure[ConnectionIO]
    context.customLinkRepository.update _ expects savedCustomLinkGroup.links.get(0).get returning 1.pure[ConnectionIO]
    context.customLinkRepository.update _ expects savedCustomLinkGroup.links.get(1).get returning 1.pure[ConnectionIO]
    context.customLinkRepository.create _ expects (1L,newLink) returning 3L.pure[ConnectionIO]

    context.customLinkGroupRepository.update _ expects (1L, updatedCustomLinkGroup) returning 1.pure[ConnectionIO]

    val updateResult =
      customLinkGroupService.updateCustomLinkGroup(1L, updatedCustomLinkGroup).unsafeRunSync()

    updateResult shouldBe ()
  }

  it should "update an existing custom link group - update an existing link" in new Context {
    val savedCustomLinkGroup = customLinkGroup(1L.some, 1L.some, 2L.some)
    val updatedCustomLink = savedCustomLinkGroup.links.head.copy(name = "Updated first link")
    val updatedCustomLinkGroup = savedCustomLinkGroup.copy(links = List(updatedCustomLink, savedCustomLinkGroup.links.get(1).get))

    context.customLinkRepository.findByCustomLinkGroupId _ expects 1L returning savedCustomLinkGroup.links.pure[ConnectionIO]
    context.customLinkRepository.update _ expects updatedCustomLink returning 1.pure[ConnectionIO]
    context.customLinkRepository.update _ expects savedCustomLinkGroup.links.get(1).get returning 1.pure[ConnectionIO]
    context.customLinkGroupRepository.update _ expects (1L, updatedCustomLinkGroup) returning 1.pure[ConnectionIO]

    val updateResult =
      customLinkGroupService.updateCustomLinkGroup(1L, updatedCustomLinkGroup).unsafeRunSync()

    updateResult shouldBe ()
  }

  it should "delete an existing custom link group" in new Context {
    context.customLinkRepository.findByCustomLinkGroupId _ expects 1L returning customLinkGroup(1L.some, 1L.some, 2L.some).links.pure[ConnectionIO]
    context.customLinkRepository.delete _ expects 1L returning ().pure[ConnectionIO]
    context.customLinkRepository.delete _ expects 2L returning ().pure[ConnectionIO]
    context.customLinkGroupRepository.delete _ expects 1L returning ().pure[ConnectionIO]

    val deleteResult = customLinkGroupService.deleteCustomLinkGroup(1L).unsafeRunSync()
    deleteResult shouldBe ()
  }

  trait Context {

    val context: AppContext[IO] = genMockContext()
    implicit val timer: Timer[IO] = new TestTimer
    implicit val clock: Clock[IO] = timer.clock

    val customLinkGroupService = new CustomLinkGroupServiceImpl[IO](context)
  }

}
