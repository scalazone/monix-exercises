package scalazone.monix.lesson5

import monix.eval.Task
import monix.execution.Scheduler

/**
  * Run with
  * {{{
  *   sbt "monix-task-exercises/runMain scalazone.monix.lesson5.ThreadManagementExercises"
  * }}}
  *
  * Test with
  * {{{
  *   sbt "monix-task-exercises/testOnly *ThreadManagementExercisesTests"
  * }}}
  */
object ThreadManagementExercises extends App {

  /** If we run this code on a single threaded `Scheduler`, `otherTask` might not execute at all.
    * Can you explain why?
    *
    * Try to fix it so `Concurrency.ex1` test in `TaskQuickStartSuite` passes.
    * Then revert your fix and try to experiment with different ExecutionModels.
    * You can change the ExecutionModel with `Task#executeWithModel`
    */
  def ex1(otherTask: Task[Unit]): Task[Unit] = {
    def forever: Task[Unit] = Task.unit.flatMap(_ => forever)

    Task.parMap2(forever, otherTask)((_, _) => ())
  }

  /** Task has a notion of default Scheduler - the one that was passed when executing it.
    *
    * If you recall, the standard practive is to move some computations to the dedicated Scheduler.
    *
    * For the exercise, execute `blockingTask` on `blockingScheduler` and keep the rest of the computation
    * on a default one.
    *
    * Try to experiment with different methods, like `executeOn` or `shift(ec)` to see the difference!
    */
  def ex2(normalTask: Task[Unit], blockingTask: Task[Unit], blockingScheduler: Scheduler): Task[Unit] =
    for {
      _ <- normalTask
      _ <- blockingTask
      _ <- normalTask
    } yield ()
}
