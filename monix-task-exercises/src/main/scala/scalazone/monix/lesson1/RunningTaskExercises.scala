package scalazone.monix.lesson1

import monix.eval.Task

/** Run with
  * sbt "monix-task-exercises/runMain scalazone.monix.lesson1.RunningTaskExercises"
  */
object RunningTaskExercises extends App {
  val exerciseTask = Task {
    println("Effect"); 20
  }

  /** Exercise 1
    *
    * Use `runToFuture` to run `exerciseTask` and print its result
    */
  val f = ???

  /** Exercise 2
    *
    * Use `runAsync` to run `exerciseTask` and print its result
    */
  val async = ???

  /** Exercise 3
    *
    * Use `runSyncUnsafe` to run `exerciseTask` and print its result
    */
  val sync = ???
}
