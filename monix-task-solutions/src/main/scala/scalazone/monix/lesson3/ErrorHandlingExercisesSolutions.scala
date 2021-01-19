package scalazone.monix.lesson3

import monix.eval.Task
import monix.execution.exceptions.DummyException

import scala.concurrent.duration.FiniteDuration

/**
  * Run with
  * {{{
  *   sbt "monix-task-solutions/runMain scalazone.monix.lesson3.ErrorHandlingExercisesSolutions"
  * }}}
  *
  * Test with
  * {{{
  *   sbt "monix-task-solutions/testOnly *ErrorHandlingExercisesTests"
  * }}}
  */
object ErrorHandlingExercisesSolutions extends App {

  /**
    * Exercise 1
    *
    * Use `onErrorHandle` to fallback to `default` in case the `Task` has an error.
    */
  def ex1[A](task: Task[A], default: A): Task[A] =
    task.onErrorHandle(_ => default)

  /**
    * Exercise 2
    *
    * Write a method that will recover with `default`,
    * but only if the exception is `DummyException`
    */
  def ex2[A](task: Task[A], default: A): Task[A] =
    task.onErrorHandleWith {
      case _: DummyException => Task.now(default)
      case other             => Task.raiseError(other)
    }

  // Alternatively task.onErrorRecover { case _: DummyException => default }

  /**
    * Exercise 3
    *
    * Use `redeem` to construct a `Task` that will
    * multiply value of `task` by 10 if it's successful but
    * it will fallback to 20 if there was an error.
    */
  def ex3(task: Task[Int]): Task[Int] =
    task.redeem(_ => 20, _ * 10)

  /**
    * Exercise 4
    *
    * Write a method which handle all errors in a Task end exposes them as `Left` in `Either`.
    *
    * There is a function called `attempt` which does it but try to implement
    * it on your own from combinators that we have learned.
    */
  def ex4[A](task: Task[A]): Task[Either[Throwable, A]] =
    task.redeem(ex => Left(ex), a => Right(a))

  /**
    * Exercise 5
    *
    * Write a recursive function which will retry Task up to `maxRetries`
    * with exponential backoff (e.g. 1s, 2s, 4s, 8s, 16s, ... ) between retries.
    *
    * Tip: Check `Task.sleep` or `Task#delayExecution` for delaying execution of a `Task`.
    * Note that unlike `Thread.sleep`, `Task.sleep` is non-blocking!
    */
  def ex5[A](source: Task[A], maxRetries: Int, firstDelay: FiniteDuration): Task[A] =
    source.onErrorHandleWith {
      case _ if maxRetries > 0 => ex5(source, maxRetries - 1, firstDelay * 2).delayExecution(firstDelay)
      case other               => Task.raiseError(other)
    }
}
