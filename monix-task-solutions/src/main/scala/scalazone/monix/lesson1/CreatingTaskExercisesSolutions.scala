package scalazone.monix.lesson1

import monix.eval.Task
import monix.execution.exceptions.DummyException

import scala.util.{Failure, Success, Try}

object CreatingTaskExercisesSolutions extends App {

  /**
    * Exercise 1
    *
    * Create a Task, that after execution will print "Hello!"
    */
  val helloTask: Task[Unit] = Task.eval(println("Hello!"))

  /**
    * Exercise 2
    *
    * Create a Task that wraps already computed Integer
    */
  val intTask: Task[Int] = Task.now(20)

  /**
    * Exercise 3
    *
    * Create a Task that fails with `monix.execution.exceptions.DummyException`
    */
  val failedTask = Task.raiseError(DummyException("dummy"))

  /**
    * Exercise 4
    *
    * Write `fromTry` method that will create a `Task`
    * from `Try`
    */
  def fromTry[A](t: Try[A]): Task[A] = t match {
    case Failure(exception) => Task.raiseError(exception)
    case Success(value)     => Task.now(value)
  }
}
