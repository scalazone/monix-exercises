package scalazone.monix.lesson2

import monix.eval.Task

import scala.util.Random

/**
  * Run with
  * {{{
  *   sbt "monix-task-exercises/runMain scalazone.monix.lesson2.TransformationExercises"
  * }}}
  *
  * Test with
  * {{{
  *   sbt "monix-task-solutions/testOnly *TransformationExercisesTests"
  * }}}
  */
object TransformationExercisesSolutions extends App {

  /**
    * Exercise 1
    *
    * Use `map` to multiply `Task` value by 10
    */
  def ex1(fa: Task[Int]): Task[Int] =
    fa.map(_ * 10)

  /**
    * Exercise 2
    *
    * Use `flatMap` or `map2` to create a `Task`
    * that will produce a sum of `a` and `b`
    */
  def ex2(a: Task[Int], b: Task[Int]): Task[Int] =
    Task.map2(a, b)(_ + _)

  /**
    * Exercise 3
    *
    * Rewrite following `Task` to for comprehension
    */
  val forTask: Task[Int] = Task(Random.nextInt(10))
    .flatMap { r1 =>
      Task(Random.nextInt(10)).flatMap { r2 =>
        Task(println(s"Generated $r1 and $r2")).map(_ => r1 * r2)
      }
    }

  val rewrittenTask: Task[Int] = for {
    r1 <- Task(Random.nextInt(10))
    r2 <- Task(Random.nextInt(10))
    _  <- Task(println(s"Generated $r1 and $r2"))
  } yield r1 * r2

  /**
    * Exercise 4
    *
    * Transform a `List` of tasks into a `Task` of a single `List`
    */
  def ex4(numbers: List[Task[Int]]): Task[List[Int]] =
    Task.sequence(numbers)

  /**
    * Exercise 5
    *
    * Write a method that will run `compute` for each number in the list and
    * then sum the results
    */
  def ex5(numbers: List[Int], compute: Int => Task[Int]): Task[Int] =
    Task.traverse(numbers)(compute).map(_.sum)

  /**
    * Exercise 6
    *
    * Use `Task` to change `compute` into a pure function
    * and then do a pure equivalent of:
    *
    * {{{
    *   val a = compute()
    *   a + a + compute()
    * }}}
    */
  def ex6(compute: () => Int): Task[Int] = {
    val pureCompute = Task { compute() }

    Task.map2(pureCompute, pureCompute) {
      case (a, b) =>
        a + a + b
    }
  }

  /**
    * Exercise 7
    *
    * Write a recursive `flatMap` loop that will run `task` repeteadly
    * until it's value is > n OR we have reached maximum number of retries.
    */
  def ex7(task: Task[Int], n: Int, maxRetries: Int): Task[Int] =
    task.flatMap { value =>
      if (maxRetries == 0 || value > n) Task.now(value)
      else ex7(task, n, maxRetries - 1)
    }
}
