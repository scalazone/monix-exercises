package scalazone.monix.app

import cats.effect.ExitCode
import monix.eval.{Task, TaskApp}
import monix.execution.Scheduler
import scalazone.monix.app.domain.{InMemShipConstructionOrderRepository, ShipyardServiceImpl}
import scalazone.monix.app.external.{InMemCrewService, InMemMarketService}

object Main extends TaskApp {
  override def run(args: List[String]): Task[ExitCode] = {
    val shipyardService = new ShipyardServiceImpl(
      new InMemMarketService,
      new InMemMarketService,
      new InMemCrewService,
      new InMemShipConstructionOrderRepository,
      Scheduler.io()
    )

    val stopSignal =
      Task {
        println("Press any key to exit ...")
        scala.io.StdIn.readLine()
      }.void

    HttpServer.start(shipyardService)(stopSignal).map(_ => ExitCode.Success)
  }
}
