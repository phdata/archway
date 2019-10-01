package io.phdata.services

import cats.effect.{ConcurrentEffect, ContextShift}
import cats.implicits._
import doobie.implicits._
import io.phdata.AppContext
import io.phdata.models.{CustomLink, CustomLinkGroup}

import scala.collection.immutable

class CustomLinkGroupServiceImpl[F[_]: ConcurrentEffect: ContextShift](
    context: AppContext[F]
) extends CustomLinkGroupService[F] {

  override def createCustomLinkGroup(customLinkGroup: CustomLinkGroup): F[Long] = {
    val result = for {
      groupId <- context.customLinkGroupRepository.create(customLinkGroup)
      _ <- customLinkGroup.links.traverse(link => context.customLinkRepository.create(groupId, link))
    } yield groupId

    result.transact(context.transactor)
  }

  override def updateCustomLinkGroup(customLinkGroupId: Long, customLinkGroup: CustomLinkGroup): F[Unit] = {
    val result = for {
      dbLinks <- context.customLinkRepository.findByCustomLinkGroupId(customLinkGroupId)
      _ <- updateCustomLinks(customLinkGroupId, customLinkGroup.links, dbLinks)
      _ <- context.customLinkGroupRepository.update(customLinkGroupId, customLinkGroup)
    } yield ()

    result.transact(context.transactor)
  }

  override def list: F[List[CustomLinkGroup]] =
    context.customLinkGroupRepository.list.transact(context.transactor)

  override def deleteCustomLinkGroup(customLinkGroupId: Long): F[Unit] = {
    val result = for {
      customLinks <- context.customLinkRepository.findByCustomLinkGroupId(customLinkGroupId)
      _ <- customLinks.traverse(link => context.customLinkRepository.delete(link.id.get))
      _ <- context.customLinkGroupRepository.delete(customLinkGroupId)
    } yield ()

    result.transact(context.transactor)
  }

  private def updateCustomLinks(
      groupId: Long,
      requestLinks: List[CustomLink],
      dbLinks: List[CustomLink]
  ) = {
    val dbLinkIds = dbLinks.map(_.id.get)
    val requestLinkIds = requestLinks.filter(_.id.isDefined).map(_.id.get)

    val linksToRemove: List[Long] = dbLinkIds.diff(requestLinkIds)
    val linksToUpdate: List[CustomLink] =
      requestLinks.filter(link => link.id.isDefined && dbLinkIds.intersect(requestLinkIds).contains(link.id.get))
    val linksToCreate: List[CustomLink] = requestLinks.filter(_.id.isEmpty)

    for {
      _ <- linksToRemove.traverse(id => context.customLinkRepository.delete(id))
      _ <- linksToUpdate.traverse(link => context.customLinkRepository.update(link))
      _ <- linksToCreate.traverse(link => context.customLinkRepository.create(groupId, link))
    } yield ()
  }

}
