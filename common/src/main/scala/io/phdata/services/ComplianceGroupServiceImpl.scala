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
      _ <- updateComplianceQuestions(complianceGroupId, complianceGroup.questions, dbQuestions)
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
      groupId: Long,
      complianceGroupQuestions: List[ComplianceQuestion],
      dbQuestions: List[ComplianceQuestion]
  ) = {
    val dbQuestionIds: List[Long] = dbQuestions.map(_.id.get)
    val requestQuestionIds: List[Long] = complianceGroupQuestions.filter(_.id.isDefined).map(_.id.get)

    val questionsToRemove: List[Long] = dbQuestionIds.diff(requestQuestionIds)
    val questionsToUpdate: List[ComplianceQuestion] = dbQuestionIds
      .intersect(requestQuestionIds)
      .map(id => complianceGroupQuestions.filter(question => question.id.isDefined && question.id.get == id).head)
    val questionsToCreate: List[ComplianceQuestion] = complianceGroupQuestions.filter(_.id.isEmpty)

    for {
      _ <- questionsToRemove.traverse(id => context.complianceQuestionRepository.delete(id))
      _ <- questionsToUpdate.traverse(question => context.complianceQuestionRepository.update(question))
      _ <- questionsToCreate.traverse(question => context.complianceQuestionRepository.create(groupId, question))
    } yield ()
  }
}
