package io.phdata.services

import java.time.Instant

import cats.effect.{Clock, ConcurrentEffect, ContextShift}
import cats.implicits._
import com.typesafe.scalalogging.LazyLogging
import doobie.implicits._
import io.phdata.AppContext
import io.phdata.models.{ComplianceGroup, ComplianceQuestion}

class ComplianceGroupServiceImpl[F[_]: ConcurrentEffect: ContextShift: Clock](
    context: AppContext[F]
) extends ComplianceGroupService[F] with LazyLogging {

  override def createComplianceGroup(complianceGroup: ComplianceGroup): F[Long] = {
    val result = for {
      groupId <- context.complianceGroupRepository.create(complianceGroup)
      _ <- complianceGroup.questions.traverse(
        question => context.complianceQuestionRepository.create(groupId, question)
      )
    } yield groupId

    result.transact(context.transactor)
  }

  override def updateComplianceGroup(complianceGroupId: Long, complianceGroup: ComplianceGroup): F[Unit] = {
    val result = for {
      dbQuestions <- context.complianceQuestionRepository.findByComplianceGroupId(complianceGroupId)
      _ <- updateComplianceQuestions(complianceGroup, dbQuestions)
      _ <- context.complianceGroupRepository.update(complianceGroupId, complianceGroup)
    } yield ()

    result.transact(context.transactor)
  }

  override def list(): F[List[ComplianceGroup]] =
    context.complianceGroupRepository.list.transact(context.transactor)

  override def deleteComplianceGroup(complianceGroupId: Long): F[Unit] = {
    val result = for {
      _ <- context.complianceGroupRepository.delete(complianceGroupId)
    } yield ()

    result.transact(context.transactor)
  }

  override def loadDefaultComplianceQuestions(): F[Unit] =
    Clock[F].realTime(scala.concurrent.duration.MILLISECONDS).flatMap { time =>
      for {
        dbGroups <- list
        _ <- if (dbGroups.isEmpty) {
          ComplianceGroup.defaultGroups(Instant.ofEpochMilli(time)).traverse(group => createComplianceGroup(group)).void
        } else ().pure[F]
      } yield ()

    }

  private def updateComplianceQuestions(
      complianceGroup: ComplianceGroup,
      dbQuestions: List[ComplianceQuestion]
  ) = {
    val dbQuestionIds = dbQuestions.map(_.id.get)
    val requestQuestionIds = complianceGroup.questions.map(_.id.get)

    for {
      _ <- dbQuestionIds.diff(requestQuestionIds).traverse(id => context.complianceQuestionRepository.delete(id))
      _ <- complianceGroup.questions.traverse(question => context.complianceQuestionRepository.update(question))
      _ <- complianceGroup.questions.traverse(
        question => context.complianceQuestionRepository.create(complianceGroup.id.get, question)
      )
    } yield ()
  }
}
