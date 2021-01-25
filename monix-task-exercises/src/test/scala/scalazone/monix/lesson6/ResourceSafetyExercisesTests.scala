package scalazone.monix.lesson6

import cats.effect.concurrent.Deferred
import monix.eval.Task
import monix.execution.exceptions.DummyException
import monix.execution.schedulers.TestScheduler
import monix.execution.{ExecutionModel, Scheduler}
import scalazone.monix.BaseTestSuite

import java.io.InputStream
import scala.concurrent.duration._
import scala.concurrent.Await
import scala.util.{Failure, Success}

class ResourceSafetyExercisesTests extends BaseTestSuite {
  test("ResourceSafetyExercises.ex1") {
    implicit val s = TestScheduler()

    val successfulTask = Task.eval(20)
    val f1             = ResourceSafetyExercises.ex1(successfulTask).runToFuture
    f1.value shouldEqual Some(Success(20))

    val dummy      = DummyException("dummy")
    val failedTask = Task.raiseError(dummy)

    val f2 = ResourceSafetyExercises.ex1(failedTask).runToFuture
    s.tick(1.second)
    f2.value shouldEqual None
    s.tick(1.second)
    f2.value shouldEqual Some(Failure(dummy))

    val canceledTask = Task.never
    val f3           = ResourceSafetyExercises.ex1(canceledTask).runToFuture
    f3.cancel()
    f3.value shouldEqual None
    s.tick(1.second)

    s.state.tasks.isEmpty shouldEqual true
  }

  test("ResourceSafetyExercises.ex2") {
    implicit val s = Scheduler.global

    var id        = 0
    var wasClosed = false

    def inputStream: InputStream = {
      new InputStream {
        override def read(): Int = {
          id += 1
          id
        }

        override def close(): Unit = wasClosed = true
      }
    }

    val f1 = ResourceSafetyExercises.ex2(() => inputStream).runToFuture

    wasClosed shouldEqual true
    id shouldEqual 1
    f1.value shouldEqual Some(Success(1))

    val isCreated            = Deferred.unsafe[Task, Unit]
    val unblockIS            = Deferred.unsafe[Task, Unit]
    @volatile var wasClosed2 = false
    @volatile var wasRead2   = false

    def inputStream2: InputStream = {
      // created
      isCreated.complete(()).flatMap(_ => unblockIS.get).runSyncUnsafe()
      unblockIS.get
      new InputStream {
        override def read(): Int = {
          wasRead2 = true
          1
        }

        override def close(): Unit = {
          wasClosed2 = true
        }
      }
    }

    val f2 = ResourceSafetyExercises.ex2(() => inputStream2).executeAsync.runToFuture
    isCreated.get.map(_ => f2.cancel).flatMap(_ => unblockIS.complete(())).runSyncUnsafe()

    // TODO: remove sleep
    Thread.sleep(100)
    wasRead2 shouldEqual false
    wasClosed2 shouldEqual true

    var wasClosed3 = false
    val dummy      = DummyException("BOOM")

    def failingInputStream: InputStream = {
      new InputStream {
        override def read(): Int = throw dummy

        override def close(): Unit = wasClosed3 = true
      }
    }

    val f3 = ResourceSafetyExercises.ex2(() => failingInputStream).runToFuture
    wasClosed3 shouldEqual true
    f3.value shouldEqual Some(Failure(dummy))
  }

  test("ResourceSafetyExercises.ex3") {
    implicit val s = TestScheduler()

    var id        = 0
    var wasClosed = false

    def inputStream: InputStream = {
      new InputStream {
        override def read(): Int = {
          id += 1
          id
        }

        override def close(): Unit = wasClosed = true
      }
    }

    val f1 = ResourceSafetyExercises.ex3(() => inputStream).runToFuture

    s.tick()
    f1.value shouldEqual None

    s.tick(1.second)
    wasClosed shouldEqual true
    id shouldEqual 2
    f1.value shouldEqual Some(Success((1, 2)))

    wasClosed = false
    id = 0
    val f2 = ResourceSafetyExercises.ex3(() => inputStream).runToFuture

    s.tick()
    f1.value shouldEqual None
    f2.cancel()
    s.tick(1.second)
    f1.value shouldEqual None
    s.state.tasks.isEmpty shouldEqual true
  }
}
