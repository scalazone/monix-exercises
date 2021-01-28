package scalazone.monix.app

import cats.effect.concurrent.Deferred
import monix.eval.Task
import monix.execution.Scheduler
import scalazone.monix.app.domain.ShipyardService

import java.io.IOException
import java.net.ServerSocket

class HttpServerLevelTwoTests extends BaseTestSuite {
  val shipyardService: ShipyardService = ShipyardService.inMem(Scheduler.io())

  "HTTP Server" should {
    "close after termination" in {
      implicit val scheduler = Scheduler.global

      val host = "localhost"
      val port = randomPort()

      assertPortIsAvailable(port)

      val test: Task[Unit] =
        for {
          serverStarted <- Deferred[Task, Unit]
          stopSignal     = serverStarted.complete(()) >> Task.never
          serverFiber   <- HttpServer.start(shipyardService, host, port)(stopSignal).start
          _             <- serverStarted.get
          _             <- Task.eval(assertPortIsUsed(port))
          _             <- serverFiber.cancel
        } yield {
          assertPortIsAvailable(port)
        }

      test.runSyncUnsafe()
    }
  }

  def randomPort(): Int = {
    val socket = new ServerSocket(0)
    val port   = socket.getLocalPort
    socket.close()
    port
  }

  def assertPortIsAvailable(port: Int): Unit = {
    var socket: ServerSocket = null

    try {
      socket = new ServerSocket(port)
    } catch {
      case ex: IOException  => fail(s"port $port should be available: $ex")
      case other: Throwable => throw other
    } finally {
      if (socket != null) socket.close()
    }
  }

  def assertPortIsUsed(port: Int): Unit = {
    var socket: ServerSocket = null

    try {
      socket = new ServerSocket(port)
    } catch {
      case _: IOException   => ()
      case other: Throwable => throw other
    } finally {
      if (socket != null) {
        socket.close()
        fail(s"port $port should not be available")
      }
    }
  }
}
