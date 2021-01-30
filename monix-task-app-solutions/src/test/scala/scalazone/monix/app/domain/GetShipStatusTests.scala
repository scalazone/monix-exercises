package scalazone.monix.app.domain

import monix.eval.Task
import monix.execution.Scheduler
import monix.execution.atomic.AtomicAny
import monix.execution.schedulers.TestScheduler
import scalazone.monix.app.ShipGetStatusError._
import scalazone.monix.app._
import scalazone.monix.app.external._

import java.util.UUID
import scala.concurrent.duration.{FiniteDuration, _}
import scala.util.Success
import GetShipStatusTests._

class GetShipStatusLevelOneTests extends BaseTestSuite {
  implicit val s: Scheduler = Scheduler.global

  "ShipyardService#getShip" should {
    "return correct status for the official market" in new Fixture {
      val test = for {
        _        <- shipConstructionOrderRepository.saveOrder(officialOrder)
        received <- serviceUnderTest.getShipStatus(shipOrderIdOfficial)
      } yield {
        received shouldEqual Right(expectedStatus)
      }

      test.runSyncUnsafe()
    }

    "return correct status for the smugglers market" in new Fixture {
      val test = for {
        _        <- shipConstructionOrderRepository.saveOrder(smugglerOrder)
        received <- serviceUnderTest.getShipStatus(shipOrderIdOfficial)
      } yield {
        received shouldEqual Right(expectedStatus)
      }

      test.runSyncUnsafe()
    }

    "return MarketOrderNotFound" in new Fixture {
      val status = serviceUnderTest.getShipStatus(marketNotFoundOrderId).runSyncUnsafe()

      status shouldBe Left(MarketOrderNotFound(marketNotFoundOrder.marketOrderId))
    }

    "return CrewOrderNotFound" in new Fixture {
      val status = serviceUnderTest.getShipStatus(crewNotFoundOrderId).runSyncUnsafe()

      status shouldBe Left(CrewOrderNotFound(crewNotFoundOrder.crewOrderId))
    }

    "return ExternalOrdersNotFound" in new Fixture {
      val status = serviceUnderTest.getShipStatus(servicesNotFoundOrderId).runSyncUnsafe()

      status shouldBe Left(ExternalOrdersNotFound(servicesNotFoundOrder.marketOrderId, servicesNotFoundOrder.crewOrderId))
    }

    "return ShipOrderNotFound" in new Fixture {
      val status = serviceUnderTest.getShipStatus(notFoundOrderId).runSyncUnsafe()

      status shouldBe Left(ShipOrderNotFound(notFoundOrderId))
    }

    "return ExternalServicesFailure for unexpected errors" in new Fixture {
      val status = serviceUnderTest.getShipStatus(failingOrderId).runSyncUnsafe()

      status shouldBe Left(ExternalServicesFailure("Market unavailable"))
    }
  }

}

class GetShipStatusLevelThreeTests extends BaseTestSuite {
  "ShipyardService#getShip" should {
    "call crewService on a different thread pool" in new Fixture {
      val mainScheduler     = TestScheduler()
      val blockingScheduler = TestScheduler()

      val serviceUnderTest2 = new ShipyardServiceImpl(
        officialMarket,
        smugglersMarket,
        crewService,
        shipConstructionOrderRepository,
        blockingScheduler
      )

      val statusF = serviceUnderTest2.getShipStatus(shipOrderIdOfficial).executeAsync.runToFuture(mainScheduler)
      mainScheduler.tick()
      statusF.value shouldBe None
      blockingScheduler.tick()
      mainScheduler.tick()

      statusF.value shouldBe Some(Success(Right(expectedStatus)))
    }

    "call external services concurrently" in new Fixture {
      val mainScheduler     = TestScheduler()
      val blockingScheduler = TestScheduler()

      val serviceUnderTest2 = new ShipyardServiceImpl(
        DelayedOfficialMarketService,
        smugglersMarket,
        new TestCrewService(Map(crewOrderId -> CrewOrderStatus(10, 100)), mainScheduler, 5.second),
        shipConstructionOrderRepository,
        blockingScheduler
      )

      val statusF = serviceUnderTest2.getShipStatus(shipOrderIdOfficial).executeAsync.runToFuture(mainScheduler)
      mainScheduler.tick()
      blockingScheduler.tick()
      statusF.value shouldBe None

      mainScheduler.tick()
      assert(mainScheduler.state.tasks.size > 1, "Market and crew orders should be running concurrently")

      mainScheduler.tick(5.second)
      assert(
        mainScheduler.state.tasks.isEmpty,
        "There should be no tasks to execute after 5 seconds. Make sure both services are running concurrently"
      )

      blockingScheduler.tick()
      mainScheduler.tick()
      assert(
        mainScheduler.state.tasks.isEmpty,
        "There should be no tasks to execute after 5 seconds. Make sure both services are running concurrently"
      )
      statusF.value shouldBe Some(Success(Right(expectedStatus)))
    }
  }
}

object GetShipStatusTests {
  private[domain] class Fixture {
    val marketOrderId   = OrderId("marketOrderId")
    val smugglerOrderId = OrderId("smugglerOrderId")
    val failingOrderId  = OrderId("failingOrderId")
    val marketStatus    = MarketOrderStatus(10)

    object DummyOfficialMarketService extends MarketService {
      def prepareOrder(shipType: ShipType, guns: Int): Task[OrderId] = Task.now(marketOrderId)

      def confirmOrder(orderId: OrderId): Task[Unit] = Task.unit

      def checkOrderStatus(orderId: OrderId): Task[Option[MarketOrderStatus]] =
        if (orderId == marketOrderId) Task.some(marketStatus)
        else if (orderId == failingOrderId) Task.raiseError(new Exception("Market unavailable")).executeAsync
        else Task.none
    }

    object DelayedOfficialMarketService extends MarketService {
      def prepareOrder(shipType: ShipType, guns: Int): Task[OrderId] = Task.now(marketOrderId)

      def confirmOrder(orderId: OrderId): Task[Unit] = Task.unit

      def checkOrderStatus(orderId: OrderId): Task[Option[MarketOrderStatus]] =
        if (orderId == marketOrderId) Task.some(marketStatus).delayExecution(4.second)
        else if (orderId == failingOrderId) Task.raiseError(new Exception("Market unavailable")).executeAsync
        else Task.none
    }

    object DummySmugglersMarketService extends MarketService {
      def prepareOrder(shipType: ShipType, guns: Int): Task[OrderId] = Task.now(marketOrderId)

      def confirmOrder(orderId: OrderId): Task[Unit] = Task.unit

      def checkOrderStatus(orderId: OrderId): Task[Option[MarketOrderStatus]] =
        if (orderId == smugglerOrderId) Task.some(marketStatus)
        else if (orderId == failingOrderId) Task.raiseError(new Exception("Market unavailable")).executeAsync
        else Task.none
    }

    val crewOrderId = OrderId("crewOrderId")
    val crewStatus  = CrewOrderStatus(10, 100)

    object DummyCrewService extends CrewService {
      def hireCrew(size: Int): OrderId = crewOrderId

      def checkCrewStatus(orderId: OrderId): Option[CrewOrderStatus] = {
        if (orderId == crewOrderId) Some(crewStatus)
        else None
      }
    }

    val shipOrderIdOfficial     = OrderId("shipOrderIdOfficial")
    val shipOrderIdSmuggler     = OrderId("shipOrderIdSmuggler")
    val notFoundOrderId         = OrderId("bdmxdbfaw")
    val marketNotFoundOrderId   = OrderId("marketNotFoundOrderId")
    val crewNotFoundOrderId     = OrderId("crewNotFoundOrderId")
    val servicesNotFoundOrderId = OrderId("servicesNotFoundOrderId")

    val expectedStatus = ShipConstructionStatus(shipOrderIdOfficial, ShipType.Sloop, Rate.SixthRate, 10, 100, 10, 100)

    val officialOrder: ShipConstructionOrder =
      ShipConstructionOrder(
        marketType = MarketType.OfficialMarket,
        marketOrderId = marketOrderId,
        crewOrderId = crewOrderId,
        shipType = ShipType.Sloop,
        rate = Rate.SixthRate,
        crew = 100,
        guns = 100
      )

    val smugglerOrder = officialOrder.copy(marketType = MarketType.SmugglersMarket, marketOrderId = smugglerOrderId)

    val marketNotFoundOrder   = officialOrder.copy(marketOrderId = OrderId("wahkdwhakda"))
    val crewNotFoundOrder     = officialOrder.copy(crewOrderId = OrderId("wahkdwhakda"))
    val servicesNotFoundOrder = officialOrder.copy(marketOrderId = OrderId("wahkdwhakda"), crewOrderId = OrderId("wahkdwhakda"))
    val failingOrder          = officialOrder.copy(marketOrderId = failingOrderId)

    val officialMarket                  = DummyOfficialMarketService
    val smugglersMarket                 = DummySmugglersMarketService
    val crewService                     = DummyCrewService
    val shipConstructionOrderRepository = new InMemShipConstructionOrderRepository(
      Map(
        shipOrderIdOfficial     -> officialOrder,
        shipOrderIdSmuggler     -> smugglerOrder,
        marketNotFoundOrderId   -> marketNotFoundOrder,
        crewNotFoundOrderId     -> crewNotFoundOrder,
        servicesNotFoundOrderId -> servicesNotFoundOrder,
        failingOrderId          -> failingOrder
      )
    )

    val serviceUnderTest = new ShipyardServiceImpl(
      officialMarket,
      smugglersMarket,
      crewService,
      shipConstructionOrderRepository,
      Scheduler.io()
    )
  }
}
