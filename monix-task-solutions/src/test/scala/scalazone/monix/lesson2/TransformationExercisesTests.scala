package scalazone.monix.lesson2

import monix.eval.Task
import monix.execution.Scheduler
import scalazone.monix.BaseTestSuite

import scala.util.Success

class TransformationExercisesTests extends BaseTestSuite {
  implicit val s: Scheduler = Scheduler.global

  test("TransformationExercises.ex1") {
    val a = Task(10)

    val f = TransformationExercisesSolutions.ex1(a).runToFuture

    f.value shouldEqual Some(Success(100))
  }

  test("TransformationExercises.ex2") {
    val a = Task(10)
    val b = Task(20)

    val f = TransformationExercisesSolutions.ex2(a, b).runToFuture

    f.value shouldEqual Some(Success(30))
  }

  test("TransformationExercises.ex4") {
    val numbers = List(1, 2, 3, 4, 5, 6, 7)

    val f = TransformationExercisesSolutions.ex4(numbers.map(Task.now)).runToFuture

    f.value shouldEqual Some(Success(numbers))
  }

  test("TransformationExercises.ex5") {
    def compute: Int => Task[Int] = i => Task.now(i * 2)

    val numbers = List(1, 2, 3, 4, 5, 6, 7)

    val f = TransformationExercisesSolutions.ex5(numbers, compute).runToFuture

    f.value shouldEqual Some(Success(numbers.sum * 2))
  }

  test("TransformationExercises.ex6") {
    var i = 0

    def compute: Int = {
      i += 1
      i
    }

    val f = TransformationExercisesSolutions.ex6(() => compute).runToFuture

    f.value shouldEqual Some(Success(4))
  }

  test("TransformationExercises.ex7 should return after value > n") {
    var i = 0

    val task: Task[Int] = Task.eval {
      i += 1
      i
    }

    val f = TransformationExercisesSolutions.ex7(task, 3, 1000).runToFuture

    f.value shouldEqual Some(Success(4))
    i shouldEqual 4
  }

  test("TransformationExercises.ex7 should return after maxRetries is reached") {
    var i = 0

    val task: Task[Int] = Task.eval {
      i += 1
      i
    }

    val f = TransformationExercisesSolutions.ex7(task, 1000, 10).runToFuture

    f.value shouldEqual Some(Success(11))
    i shouldEqual 11
  }
}
