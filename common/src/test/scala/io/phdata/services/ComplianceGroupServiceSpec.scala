package io.phdata.services

import cats.effect.{Clock, ContextShift, IO, Timer}
import cats.implicits._
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import io.phdata.AppContext
import io.phdata.models.ComplianceQuestion
import io.phdata.test.fixtures.{AppContextProvider, DBTest, complianceGroup, _}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.ExecutionContext

class ComplianceGroupServiceSpec extends FlatSpec with Matchers with DBTest with MockFactory with AppContextProvider {

  override implicit val contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  behavior of "ComplianceGroup Service"

  it should "list compliance groups" in new Context {
    context.complianceGroupRepository.list _ expects () returning List(complianceGroup()).pure[ConnectionIO]

    val groups = complianceGroupService.list().unsafeRunSync()
    groups shouldBe List(complianceGroup())
  }

  it should "create a new compliance group" in new Context {
    context.complianceGroupRepository.create _ expects complianceGroup() returning 1L.pure[ConnectionIO]
    context.complianceQuestionRepository.create _ expects (1, complianceGroup().questions.get(0).get) returning 1L.pure[ConnectionIO]
    context.complianceQuestionRepository.create _ expects (1, complianceGroup().questions.get(1).get) returning 2L.pure[ConnectionIO]

    val creationResult = complianceGroupService.createComplianceGroup(complianceGroup()).unsafeRunSync()
    creationResult shouldBe 1L
  }

  it should "update an existing compliance group - change name" in new Context {
    val savedComplianceGroup = complianceGroup(1L.some, 1L.some, 2L.some)
    val updatedComplianceGroup = savedComplianceGroup.copy(name = "Updated group name")

    context.complianceQuestionRepository.findByComplianceGroupId _ expects 1L returning savedComplianceGroup.questions.pure[ConnectionIO]
    context.complianceQuestionRepository.update _ expects savedComplianceGroup.questions.get(0).get returning 1.pure[ConnectionIO]
    context.complianceQuestionRepository.update _ expects savedComplianceGroup.questions.get(1).get returning 1.pure[ConnectionIO]
    context.complianceGroupRepository.update _ expects (1L, updatedComplianceGroup) returning 1.pure[ConnectionIO]

    val updateResult =
      complianceGroupService.updateComplianceGroup(1L, updatedComplianceGroup).unsafeRunSync()
    updateResult shouldBe ()
  }

  it should "update an existing compliance group - remove one of questions" in new Context {
    val savedComplianceGroup = complianceGroup(1L.some, 1L.some, 2L.some)
    val updatedComplianceGroup = savedComplianceGroup.copy(questions = List(savedComplianceGroup.questions.head))

    context.complianceQuestionRepository.findByComplianceGroupId _ expects 1L returning savedComplianceGroup.questions.pure[ConnectionIO]
    context.complianceQuestionRepository.update _ expects savedComplianceGroup.questions.get(0).get returning 1.pure[ConnectionIO]
    context.complianceQuestionRepository.delete _ expects 2L returning ().pure[ConnectionIO]
    context.complianceGroupRepository.update _ expects (1L, updatedComplianceGroup) returning 1.pure[ConnectionIO]

    val updateResult =
      complianceGroupService.updateComplianceGroup(1L, updatedComplianceGroup).unsafeRunSync()
    updateResult shouldBe ()
  }

  it should "update an existing compliance group - add a new question" in new Context {
    val savedComplianceGroup = complianceGroup(1L.some, 1L.some, 2L.some)
    val newQuestion = ComplianceQuestion("New question", "Tom", testTimer.instant, savedComplianceGroup.id)
    val updatedComplianceGroup = savedComplianceGroup.copy(questions = savedComplianceGroup.questions ++ List(newQuestion))

    context.complianceQuestionRepository.findByComplianceGroupId _ expects 1L returning savedComplianceGroup.questions.pure[ConnectionIO]
    context.complianceQuestionRepository.update _ expects savedComplianceGroup.questions.get(0).get returning 1.pure[ConnectionIO]
    context.complianceQuestionRepository.update _ expects savedComplianceGroup.questions.get(1).get returning 1.pure[ConnectionIO]
    context.complianceQuestionRepository.create _ expects (1L,newQuestion) returning 3L.pure[ConnectionIO]
    context.complianceGroupRepository.update _ expects (1L, updatedComplianceGroup) returning 1.pure[ConnectionIO]
    val updateResult =
      complianceGroupService.updateComplianceGroup(1L, updatedComplianceGroup).unsafeRunSync()
    updateResult shouldBe ()
  }

  it should "update an existing compliance group - update an existing question" in new Context {
    val savedComplianceGroup = complianceGroup(1L.some, 1L.some, 2L.some)
    val updatedComplianceQuestion = savedComplianceGroup.questions.head.copy(question = "Updated question")
    val updatedComplianceGroup = savedComplianceGroup.copy(questions = List(updatedComplianceQuestion, savedComplianceGroup.questions.get(1).get))
    context.complianceQuestionRepository.findByComplianceGroupId _ expects 1L returning savedComplianceGroup.questions.pure[ConnectionIO]
    context.complianceQuestionRepository.update _ expects updatedComplianceQuestion returning 1.pure[ConnectionIO]
    context.complianceQuestionRepository.update _ expects savedComplianceGroup.questions.get(1).get returning 1.pure[ConnectionIO]
    context.complianceGroupRepository.update _ expects (1L, updatedComplianceGroup) returning 1.pure[ConnectionIO]
    val updateResult =
      complianceGroupService.updateComplianceGroup(1L, updatedComplianceGroup).unsafeRunSync()
    updateResult shouldBe ()
  }

  it should "delete an existing compliance group" in new Context {
    val savedComplianceGroup = complianceGroup(1L.some, 1L.some, 2L.some)

    context.complianceGroupRepository.delete _ expects 1L returning ().pure[ConnectionIO]

    val deleteResult = complianceGroupService.deleteComplianceGroup(1L).unsafeRunSync()
    deleteResult shouldBe ()
  }

  trait Context {

    val context: AppContext[IO] = genMockContext()
    implicit val timer: Timer[IO] = new TestTimer
    implicit val clock: Clock[IO] = timer.clock

    val complianceGroupService = new ComplianceGroupServiceImpl[IO](context)
  }
}
