package scalazone.monix.lesson1

import monix.eval.Task
import monix.execution.Scheduler

object RunningTaskExercisesSolutions extends App {
  val exerciseTask = Task {
    println("Effect"); 20
  }

  implicit val s: Scheduler = Scheduler.Implicits.global

  /** Exercise 1
    *
    * Use `runToFuture` to run `exerciseTask` and print its result
    */
  val f = exerciseTask.runToFuture.map(println)

  /** Exercise 2
    *
    * Use `runAsync` to run `exerciseTask` and print its result
    */
  val async = exerciseTask.runAsync {
    case Left(value)  => println(s"Task has encountered an error: $value")
    case Right(value) => println(s"Task has finished with a successful value: $value")
  }

  /** Exercise 3
    *
    * Use `runSyncUnsafe` to run `exerciseTask` and print its result
    */
  val sync = exerciseTask.runSyncUnsafe()
  println(sync)
}
