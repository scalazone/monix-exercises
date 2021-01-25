package scalazone.monix.lesson4

import monix.eval.Task
import monix.execution.Scheduler
import monix.execution.exceptions.DummyException
import monix.execution.schedulers.TestScheduler
import scalazone.monix.BaseTestSuite

import scala.util.Success
import scala.concurrent.duration._

class ConcurrencyExercisesTests extends BaseTestSuite {
  test("ConcurrencyExercises.ex1") {
    implicit val s = TestScheduler()

    val a = Task(10).delayExecution(5.second)
    val b = Task(20).delayExecution(10.second)

    val f = ConcurrencyExercises.ex1(a, b).runToFuture

    s.tick(10.second)
    f.value shouldEqual Some(Success(30))
  }

  test("ConcurrencyExercises.ex2") {
    implicit val s = TestScheduler()

    def foo(i: Int): Task[Int] =
      if (i % 2 == 0) Task.raiseError(DummyException("error"))
      else Task.now(i).delayExecution(1.second)

    val tasks: List[Task[Int]] = List(foo(1), foo(2), foo(3), foo(4), foo(5))

    val f = ConcurrencyExercises.ex2(tasks).runToFuture

    s.tick(1.second)
    f.value shouldEqual Some(Success(List(Some(1), None, Some(3), None, Some(5))))
    s.state.lastReportedError shouldEqual null
  }

  test("ConcurrencyExercises.ex3") {
    implicit val s = TestScheduler()

    def foo(i: Int): Task[Int] =
      if (i % 2 == 0) Task.raiseError(DummyException("error"))
      else Task.now(i).delayExecution(1.second)

    val tasks: List[Task[Unit]] = List(foo(1), foo(2), foo(3), foo(4), foo(5)).map(_.void)

    val f = ConcurrencyExercises.ex3(tasks).runToFuture

    s.tick(1.second)
    f.value shouldEqual Some(Success((3, 2)))
    s.state.lastReportedError shouldEqual null
  }
}
