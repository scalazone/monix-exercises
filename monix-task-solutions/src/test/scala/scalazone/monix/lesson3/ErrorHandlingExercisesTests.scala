package scalazone.monix.lesson3

import monix.eval.Task
import monix.execution.Scheduler
import monix.execution.exceptions.DummyException
import monix.execution.schedulers.TestScheduler
import scalazone.monix.BaseTestSuite

import scala.util.{Failure, Success}
import scala.concurrent.duration._

class ErrorHandlingExercisesTests extends BaseTestSuite {
  implicit val s: Scheduler = Scheduler.global

  test("ErrorHandlingExercises.ex1 should recover from DummyException") {
    val a = Task.raiseError(DummyException("BOOM"))

    val f = ErrorHandlingExercisesSolutions.ex1(a, 100).runToFuture

    f.value shouldEqual Some(Success(100))
  }

  test("ErrorHandlingExercises.ex1 should recover from other errors") {
    val a = Task.raiseError(new Exception("BOOM"))

    val f = ErrorHandlingExercisesSolutions.ex1(a, 100).runToFuture

    f.value shouldEqual Some(Success(100))
  }

  test("ErrorHandlingExercises.ex1 should not do anything for successful tasks") {
    val a = Task.now(200)

    val f = ErrorHandlingExercisesSolutions.ex1(a, 100).runToFuture

    f.value shouldEqual Some(Success(200))
  }

  test("ErrorHandlingExercises.ex2 should recover from DummyException") {
    val dummy = Task.raiseError(DummyException("BOOM"))

    val f = ErrorHandlingExercisesSolutions.ex2(dummy, 100).runToFuture

    f.value shouldEqual Some(Success(100))
  }

  test("ErrorHandlingExercises.ex2 should not recover from other exceptions") {
    val ex = new Exception("BOOM")
    val a  = Task.raiseError(ex)

    val f = ErrorHandlingExercisesSolutions.ex2(a, 100).runToFuture

    f.value shouldEqual Some(Failure(ex))
  }

  test("ErrorHandlingExercises.ex2 should not do anything for successful errors") {
    val a = Task.now(200)

    val f = ErrorHandlingExercisesSolutions.ex2(a, 100).runToFuture

    f.value shouldEqual Some(Success(200))
  }

  test("ErrorHandlingExercises.ex3 should recover from exceptions") {
    val ex = new Exception("BOOM")
    val a  = Task.raiseError(ex)

    val f = ErrorHandlingExercisesSolutions.ex3(a).runToFuture

    f.value shouldEqual Some(Success(20))
  }

  test("ErrorHandlingExercises.ex3 should multiply successful results") {
    val a = Task.now(200)

    val f = ErrorHandlingExercisesSolutions.ex3(a).runToFuture

    f.value shouldEqual Some(Success(200 * 10))
  }

  test("ErrorHandlingExercises.ex4 should expose exceptions") {
    val ex = new Exception("BOOM")
    val a  = Task.raiseError(ex)

    val f = ErrorHandlingExercisesSolutions.ex4(a).runToFuture

    f.value shouldEqual Some(Success(Left(ex)))
  }

  test("ErrorHandlingExercises.ex4 should expose successful results") {
    val a = Task.now(200)

    val f = ErrorHandlingExercisesSolutions.ex4(a).runToFuture

    f.value shouldEqual Some(Success(Right(200)))
  }

  test("ErrorHandlingExercises.ex5 should return immediately for success") {
    val testS           = TestScheduler()
    val task: Task[Int] = Task.now(4)

    val f = ErrorHandlingExercisesSolutions.ex5(task, 3, 1.second).runToFuture(testS)

    f.value shouldEqual Some(Success(4))
  }

  test("ErrorHandlingExercises.ex5 should fail after maxRetries is reached") {
    val testS           = TestScheduler()
    val dummy           = DummyException("BOOM")
    val task: Task[Int] = Task.raiseError(dummy)

    val f = ErrorHandlingExercisesSolutions.ex5(task, 5, 1.second).runToFuture(testS)

    testS.tick(1.second)
    f.value shouldEqual None

    testS.tick(2.second)
    f.value shouldEqual None

    testS.tick(4.second)
    f.value shouldEqual None

    testS.tick(8.second)
    f.value shouldEqual None

    testS.tick(16.second)
    f.value shouldEqual Some(Failure(dummy))
  }

  test("ErrorHandlingExercises.ex5 should succeed after few retries") {
    val testS           = TestScheduler()
    val dummy           = DummyException("BOOM")
    var shouldFail      = true
    val task: Task[Int] = Task.suspend {
      if (shouldFail) Task.raiseError(dummy)
      else Task.now(10)
    }

    val f = ErrorHandlingExercisesSolutions.ex5(task, 5, 1.second).runToFuture(testS)

    testS.tick(1.second)
    f.value shouldEqual None

    testS.tick(2.second)
    f.value shouldEqual None

    testS.tick(4.second)
    f.value shouldEqual None

    shouldFail = false
    testS.tick()
    f.value shouldEqual None

    testS.tick(8.second)
    f.value shouldEqual Some(Success(10))
  }
}
