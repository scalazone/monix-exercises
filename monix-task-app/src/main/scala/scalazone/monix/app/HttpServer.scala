package scalazone.monix.app

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import monix.eval.Task
import scalazone.monix.app.api.Endpoints
import scalazone.monix.app.domain.ShipyardService

import scala.concurrent.duration._

object HttpServer {
  def start(
      shipyardService: ShipyardService,
      host: String = "localhost",
      port: Int = 8080
  )(stopSignal: Task[Unit] = Task.never): Task[Unit] =
    Task.suspend {
      implicit val actorSystem: ActorSystem = ActorSystem()
      /** EXERCISE LEVEL 2
       *
       * Browse `Task` API to find a builder that could inject that `Scheduler` without requiring it in the `HttpServer.start` signature
       * and pass it to `Endpoints.createRoutes`
       */
      val routes                            = Endpoints.createRoutes(shipyardService)(???)

      /** EXERCISE LEVEL 2
       *
       * Create a `Task` from `Future`
       */
      val serverTask: Task[Http.ServerBinding] = {
        Http().newServerAt(host, port).bindFlow(routes)
        ???
      }

      /** EXERCISE LEVEL 2
       *
       * `serverTask` acts as a resource:
       * New server binding is acquired, then used for the duration of the application, and released when application shuts down.
       * 
       * Make sure the server binding is properly terminated, and the actor system terminated 
       * regardless if`Task` had an error, had finished, or had been cancelled.
       */
      serverTask.flatMap { serverBinding =>
        Task.suspend {
          println(s"Go to: http://$host:$port")
          stopSignal
            .flatMap { _ =>
              Task.fromFuture(serverBinding.terminate(5.second)) >> Task.fromFuture(actorSystem.terminate()).void
            }
        }
      }
    }
}
