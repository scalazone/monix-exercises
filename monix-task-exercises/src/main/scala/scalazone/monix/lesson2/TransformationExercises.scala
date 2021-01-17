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
  *   sbt "monix-task-exercises/testOnly *TransformationExercisesTests"
  * }}}
  */
object TransformationExercises extends App {

  /**
    * Exercise 1
    *
    * Use `map` to multiply `Task` value by 10
    */
  def ex1(fa: Task[Int]): Task[Int] = ???

  /**
    * Exercise 2
    *
    * Use `flatMap` or `map2` to create a `Task`
    * that will produce a sum of `a` and `b`
    */
  def ex2(a: Task[Int], b: Task[Int]): Task[Int] = ???

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

  /**
    * Exercise 4
    *
    * Transform a `List` of tasks into a `Task` of a single `List`
    */
  def ex4(numbers: List[Task[Int]]): Task[List[Int]] = ???

  /**
    * Exercise 5
    *
    * Write a method that will run `compute` for each number in the list and
    * then sum the results
    */
  def ex5(numbers: List[Int], compute: Int => Task[Int]): Task[Int] = ???

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
  def ex6(compute: () => Int): Task[Int] = ???

  /**
    * Exercise 7
    *
    * Write a recursive `flatMap` loop that will run `task` repeteadly
    * until it's value is > n OR we have reached maximum number of retries.
    */
  def ex7(task: Task[Int], n: Int, maxRetries: Int): Task[Int] = ???
}
