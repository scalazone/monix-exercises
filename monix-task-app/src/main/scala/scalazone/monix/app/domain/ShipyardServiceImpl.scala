package scalazone.monix.app.domain

import monix.eval.Task
import monix.execution.Scheduler
import scalazone.monix.app.ShipConstructionError._
import scalazone.monix.app.ShipGetStatusError._
import scalazone.monix.app._
import scalazone.monix.app.domain.ShipyardServiceImpl.{CrewServiceError, MarketServiceError}
import scalazone.monix.app.external._

import scala.util.control.NoStackTrace

final class ShipyardServiceImpl(
    officialMarket: MarketService,
    smugglersMarket: MarketService,
    crewService: CrewService,
    shipConstructionOrderRepository: ShipConstructionOrderRepository,
    blockingScheduler: Scheduler
) extends ShipyardService {
  def getShipStatus(orderId: OrderId): Task[Either[ShipGetStatusError, ShipConstructionStatus]] =
    shipConstructionOrderRepository.findOrder(orderId).flatMap {
      case Some(order) =>
        /** EXERCISE LEVEL 1
          *
          * Call correct Market based on the order.
          */
        val checkMarketStatus: Task[Option[MarketOrderStatus]] = ???

        /** EXERCISE LEVEL 1
          *
          * Call CrewService to check the crew status.
          * Note that the service is side-effectful, so make sure to evaluate it in the `Task` context.
          *
          * EXERCISE LEVEL 3
          *
          * Move crewService execution to the `blockingScheduler`
          */
        val checkCrewStatus: Task[Option[CrewOrderStatus]] = ???

        /** EXERCISE LEVEL 1
          *
          * Combine the results of both calls to create a ShipConstructionStatus if they are successful.
          *
          * EXERCISE LEVEL 3
          *
          * Call both services concurrently.
          */

        /** EXERCISE LEVEL 1
          *
          * If any of the orders is not found, map it to the respective error
          */

        /** EXERCISE LEVEL 1
          *
          * Calls to the services can also fail the `Task` for other reasons.
          * Promote them to `ExternalServicesFailure` that is returned as a value.
          */

        ???

      /** EXERCISE LEVEL 1
        *
        * Return a proper ShipGetStatusError as a value if the order is not found.
        */
      case None        => ???
    }

  def constructShip(orderRequest: ShipConstructionOrderRequest): Task[Either[ShipConstructionError, OrderId]] =
    Rate.fromOrder(orderRequest) match {
      /** EXERCISE LEVEL 1
        *
        * Return a proper ShipConstructionError as a value if the Rate validation fails.
        */
      case Left(error)     => ???
      case Right(shipRate) =>
        /** EXERCISE LEVEL 1
          *
          * If `orderFromMarkets` fails, lift the error to `MarketServiceError``
          */
        val marketTask: Task[(MarketType, OrderId)] =
          orderFromMarkets(orderRequest)

        val crewTask: Task[OrderId] =
          Task(
            crewService
              .hireCrew(orderRequest.crew)
          )

        /** EXERCISE LEVEL 3
          *
          * `CrewService.hireCrew` is an external service that returns a synchronous value which
          * indicates that it might be blocking. We don't want to do that on our main thread pool.
          *
          * Let's do something about it.
          */
        
        /** EXERCISE LEVEL 1
          *
          * If `hireCrew` fails, lift the error to `CrewServiceError``
          */

        /** EXERCISE LEVEL 1
         *
         * In case the `Task` fails, we would like to return expected (MarketServiceError and CrewServiceError) errors 
         * as explicit ShipConstructionError values in `Either`, and fail for the other ones.
         * Fill [[transformErrors]] for transformation function.
         *
         * If both tasks are successful, save the construction order.
         *
         * 
         * EXERCISE LEVEL 3
         *
         * Run both tasks concurrently.
         */
        Task
          .map2(marketTask, crewTask) {
            ???
          }
    }

  /** EXERCISE LEVEL 1
    *
    * Prepare order in the `officialMarket` and then confirm it.
    *
    * EXERCISE LEVEL 3
    *
    * Call both markets concurrently and choose the first one to respond.
    *
    * EXERCISE LEVEL 3++
    *
    * If the first Market to respond fails, the other one could still return a successful result but
    * it will be cancelled. Ideally, we would wait for the first success.
    * The solution might be a bit out of scope (unless there is a dedicated operator to it in Monix by the time you're reading it ;) )
    * of the beginner level so consider it a completely optional exercise!
    */
  private def orderFromMarkets(orderRequest: ShipConstructionOrderRequest): Task[(MarketType, OrderId)] = ???
  
  private val transformErrors: Throwable => Task[Either[ShipConstructionError, OrderId]] = {
    case MarketServiceError(msg) => ???
    case CrewServiceError(msg)   => ???
    case other                   => ???
  }

  private def saveConstructionOrder(
      orderRequest: ShipConstructionOrderRequest,
      shipRate: Rate,
      marketType: MarketType,
      marketOrderId: OrderId,
      crewOrderId: OrderId
  ): Task[Either[ShipConstructionError, OrderId]] = {
    val order = ShipConstructionOrder(
      marketType = marketType,
      marketOrderId = marketOrderId,
      crewOrderId = crewOrderId,
      shipType = orderRequest.shipType,
      rate = shipRate,
      crew = orderRequest.crew,
      guns = orderRequest.guns
    )

    shipConstructionOrderRepository.saveOrder(order).map(Right(_))
  }
}

object ShipyardServiceImpl {
  final case class CrewServiceError(msg: String) extends Exception(msg) with NoStackTrace

  final case class MarketServiceError(msg: String) extends Exception(msg) with NoStackTrace
}
