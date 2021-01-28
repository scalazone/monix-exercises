package scalazone.monix.app.api

import akka.http.scaladsl.server.{Directives, Route}
import io.circe.{Decoder, Encoder, HCursor, Json}
import monix.execution.Scheduler
import scalazone.monix.app.ShipConstructionError
import scalazone.monix.app._
import scalazone.monix.app.domain.ShipyardService
import sttp.tapir._
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.akkahttp._

object Endpoints extends ShipyardErrorMappers {
  private val getShipConstructionStatus: Endpoint[OrderId, ApiError, ShipConstructionStatus, Any] =
    endpoint.get
      .in("orders")
      .in(path[OrderId])
      .out(jsonBody[ShipConstructionStatus])
      .errorOut(jsonBody[ApiError])

  private val orderShipConstruction: Endpoint[ShipConstructionOrderRequest, ApiError, OrderId, Any] =
    endpoint.post
      .in("orders")
      .in(jsonBody[ShipConstructionOrderRequest])
      .out(jsonBody[OrderId])
      .errorOut(jsonBody[ApiError])

  /** EXERCISE LEVEL 2
    *
    * Connect the ShipyardService logic to correct routes.
    * You will have to take care of two tasks:
    * - Mapping specific Domain Errors to ApiError (tip: Look at [[ShipyardErrorMappers]].)
    * - Transforming a `Task` into `Future` that is expected by Akka HTTP
    */
  def createRoutes(service: ShipyardService)(implicit s: Scheduler): Route = {
    val getShipConstructionStatusRoute: Route =
      getShipConstructionStatus.toRoute(id => service.getShipStatus(id).map(_.left.map(getShipStatusErrorMapping)).runToFuture)

    val orderShipConstructionRoute: Route =
      orderShipConstruction.toRoute(orderRequest => service.constructShip(orderRequest).map(_.left.map(shipConstructionErrorMapping)).runToFuture)

    Directives.concat(getShipConstructionStatusRoute, orderShipConstructionRoute)
  }

}
