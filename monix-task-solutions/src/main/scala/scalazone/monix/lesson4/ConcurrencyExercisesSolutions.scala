package scalazone.monix.lesson4

import monix.eval.Task

/** Run with
  * {{{
  *   sbt "monix-task-solutions/runMain scalazone.monix.lesson4.ConcurrencyExercisesSolutions"
  * }}}
  *
  * Test with
  * {{{
  *   sbt "monix-task-solutions/testOnly *ConcurrencyExercisesTests"
  * }}}
  */
object ConcurrencyExercisesSolutions extends App {

  /** Compute a sum of both tasks in parallel
    */
  def ex1(fa: Task[Int], fb: Task[Int]): Task[Int] = Task.parMap2(fa, fb)(_ + _)

  /** If we are executing multiple tasks concurrently, it is possible that one of them could fail.
    * The default behavior is that all other task will be canceled and the Task will return with
    * the first failure (other possible failures will be reported to Scheduler).
    *
    * What if we want to wait for all tasks to finish, regardless of the outcome?
    * Fortunately, effect types like Monix Task make such modifications very easy!
    *
    * Try to process all `tasks` concurrently in a way that would wait for all of them to finish and gather their results.
    */
  def ex2(tasks: List[Task[Int]]): Task[List[Option[Int]]] =
    Task.parTraverse(tasks)(_.redeem(_ => None, Some(_)))

  /** Modify solution to `ex2` to return `Task[(Int, Int)]`
    * where the tuple is (numberOfSuccesses, numberOfFailures).
    */
  def ex3(tasks: List[Task[Unit]]): Task[(Int, Int)] =
    Task.parTraverse(tasks)(_.attempt).map { list =>
      val (successes, failures) = list.partition(_.isRight)
      (successes.size, failures.size)
    }
}
