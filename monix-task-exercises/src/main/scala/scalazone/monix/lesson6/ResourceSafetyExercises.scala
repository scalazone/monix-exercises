package scalazone.monix.lesson6

import cats.effect.ExitCase
import monix.eval.Task
import scala.concurrent.duration._
import java.io.InputStream

/** Run with
  * {{{
  *   sbt "monix-task-exercises/runMain scalazone.monix.lesson6.ResourceSafetyExercises"
  * }}}
  *
  * Test with
  * {{{
  *   sbt "monix-task-exercises/testOnly *ResourceSafetyExercisesTests"
  * }}}
  */
object ResourceSafetyExercises extends App {

  /** Add finalizers to the task with the following rules, depending on the exit case:
    * a) if the `Task` is successful, do nothing
    * b) if the `Task` has returned an error, sleep for 2 seconds
    * c) if the `Task` has been canceled, sleep for 1 second
    */
  def ex1(fa: Task[Int]): Task[Int] = ???

  /** Use `Task#bracket` to re-implement the following code.
    */
  def ex2(mkInputStream: () => InputStream): Task[Int] = Task {
    val in = mkInputStream()

    try in.read()
    finally in.close()
  }

  /** Modify the solution to the last exercise
    * but now read twice before closing the input stream AND add a 1 second delay between reads.
    *
    * Could you do it with a classic try-finally construct, without blocking any Threads?
    */
  def ex3(mkInputStream: () => InputStream): Task[(Int, Int)] = Task {
    val in = mkInputStream()

    try {
      val i1 = in.read()
      // Add 1 second delay
      val i2 = in.read()
      (i1, i2)
    } finally in.close()
  }
}
