package scalazone.monix.app.api

import scalazone.monix.app.ShipGetStatusError._
import scalazone.monix.app.{ApiError, ShipConstructionError, ShipGetStatusError}

/** EXERCISE LEVEL 2
  *
  * Fill in missing mappings.
  */
trait ShipyardErrorMappers {
  val getShipStatusErrorMapping: ShipGetStatusError => ApiError = {
    case ShipOrderNotFound(orderId)                         => ApiError(s"ShipOrder ${orderId.id} not found")
    case CrewOrderNotFound(crewOrderId)                     => ApiError(s"Crew Order ${crewOrderId.id} not found")
    case MarketOrderNotFound(marketOrderId)                 => ApiError(s"Market order ${marketOrderId.id} not found")
    case ExternalOrdersNotFound(marketOrderId, crewOrderId) =>
      ApiError(s"Market order ${marketOrderId.id} and Crew Order ${crewOrderId.id} not found")
    case ExternalServicesFailure(msg)                       => ApiError(s"External failure $msg")
  }

  val shipConstructionErrorMapping: ShipConstructionError => ApiError = {
    case ShipConstructionError.ValidationError(msg)             => ApiError(msg)
    case ShipConstructionError.CouldNotOrderMaterialsError(msg) => ApiError(msg)
    case ShipConstructionError.CouldNotHireCrewError(msg)       => ApiError(msg)
  }
}
