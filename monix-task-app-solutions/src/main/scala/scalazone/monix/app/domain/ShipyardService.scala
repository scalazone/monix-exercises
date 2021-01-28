package scalazone.monix.app.domain

import monix.eval.Task
import monix.execution.Scheduler
import scalazone.monix.app.external.{InMemCrewService, InMemMarketService}
import scalazone.monix.app.{
  OrderId,
  ShipConstructionError,
  ShipConstructionOrderRequest,
  ShipConstructionStatus,
  ShipGetStatusError
}

trait ShipyardService {
  def getShipStatus(orderId: OrderId): Task[Either[ShipGetStatusError, ShipConstructionStatus]]

  def constructShip(orderRequest: ShipConstructionOrderRequest): Task[Either[ShipConstructionError, OrderId]]
}

object ShipyardService {
  def inMem(blockingScheduler: Scheduler): ShipyardService = new ShipyardServiceImpl(
    officialMarket = new InMemMarketService,
    smugglersMarket = new InMemMarketService,
    crewService = new InMemCrewService,
    shipConstructionOrderRepository = new InMemShipConstructionOrderRepository,
    blockingScheduler = blockingScheduler
  )
}
