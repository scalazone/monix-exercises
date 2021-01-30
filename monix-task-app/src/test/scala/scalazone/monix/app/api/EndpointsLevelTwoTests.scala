package scalazone.monix.app.api

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.generic.auto._
import monix.eval.Task
import monix.execution.Scheduler
import scalazone.monix.app._
import scalazone.monix.app.domain.ShipyardService

class EndpointsLevelTwoTests extends BaseTestSuite with ScalatestRouteTest with FailFastCirceSupport {

  val expectedStatusResponse: ShipConstructionStatus = ShipConstructionStatus(
    orderId = OrderId("1"),
    shipType = ShipType.Frigate,
    rate = Rate.FirstRate,
    crewReady = 90,
    crewTotal = 100,
    gunsReady = 90,
    gunsTotal = 100
  )

  val expectedConstructionResponse: OrderId = OrderId("1")

  val shipyardService: ShipyardService = new ShipyardService {
    override def getShipStatus(orderId: OrderId): Task[Either[ShipGetStatusError, ShipConstructionStatus]] =
      Task.right(expectedStatusResponse)

    override def constructShip(orderRequest: ShipConstructionOrderRequest): Task[Either[ShipConstructionError, OrderId]] =
      Task.right(expectedConstructionResponse)
  }
  implicit val scheduler: Scheduler    = Scheduler.global

  val routes: Route = Endpoints.createRoutes(shipyardService)

  "Endpoints" should {
    "GET /orders/{orderID}" in {
      Get(s"/orders/1") ~> routes ~> check {
        status shouldEqual StatusCodes.OK
        entityAs[ShipConstructionStatus] shouldBe expectedStatusResponse
      }
    }

    "POST /orders" in {
      val request = ShipConstructionOrderRequest(
        shipType = ShipType.ShipOfTheLine,
        crew = 100,
        guns = 100
      )

      Post(s"/orders", request) ~> routes ~> check {
        status shouldEqual StatusCodes.OK
        entityAs[OrderId] shouldBe expectedConstructionResponse
      }
    }
  }
}
