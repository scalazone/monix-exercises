package scalazone.monix.lesson5

import monix.eval.Task
import monix.execution.{ExecutionModel, Scheduler}
import monix.execution.exceptions.DummyException
import monix.execution.schedulers.TestScheduler
import scalazone.monix.BaseTestSuite

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Success

class ThreadManagementExercisesTests extends BaseTestSuite {
  test("ThreadManagementExercises.ex1") {
    implicit val s: Scheduler = Scheduler
      .singleThread("test")
      .withExecutionModel(ExecutionModel.SynchronousExecution)

    val dummy = DummyException("dummy")
    val task  = Task.raiseError(dummy).executeAsync

    val f = ThreadManagementExercises.ex1(task).attempt.runToFuture

    Await.result(f, 2.second) shouldEqual Left(dummy)
  }

  test("ThreadManagementExercises.ex2") {
    implicit val s1 = TestScheduler()
    val s2          = TestScheduler()

    var effect         = 0
    var blockingEffect = 0

    val normalTask   = Task { effect += 1 }
    val blockingTask = Task { blockingEffect += 1 }

    val f = ThreadManagementExercises.ex2(normalTask, blockingTask, s2).runToFuture

    // first normalTask will run synchronously
    f.value shouldEqual None
    effect shouldEqual 1
    blockingEffect shouldEqual 0
    
    // No changes after scheduling everything on s1
    s1.tick()
    f.value shouldEqual None
    effect shouldEqual 1
    blockingEffect shouldEqual 0
    
    // execute blockingTask
    s2.tick()
    f.value shouldEqual None
    effect shouldEqual 1
    blockingEffect shouldEqual 1

    // execute second normalTask
    s1.tick()
    f.value shouldEqual Some(Success(()))
    effect shouldEqual 2
    blockingEffect shouldEqual 1
  }
}
