package scalazone.monix.app.domain

import monix.eval.Task
import monix.execution.Scheduler
import monix.execution.schedulers.TestScheduler
import scalazone.monix.app._
import scalazone.monix.app.external.{CrewOrderStatus, CrewService, InMemCrewService, InMemMarketService, MarketOrderStatus, MarketService}

import scala.concurrent.duration._
import scala.util.Success
import ConstructShipTests._

class ConstructShipLevelOneTests extends BaseTestSuite {
  implicit val s: Scheduler = Scheduler.global
  "ShipyardService#constructShip" should {
    "work in a happy path" in new Fixture {
      val test = for {
        received <- serviceUnderTest.constructShip(validOrderRequest)
      } yield {
        received shouldEqual Right(shipOrderId)
      }

      test.runSyncUnsafe()
    }

    "return ValidationError" in new Fixture {
      val received = serviceUnderTest.constructShip(invalidOrderRequest).runSyncUnsafe()

      received match {
        case Left(error)  => error shouldBe a[ShipConstructionError.ValidationError]
        case Right(value) => fail(s"unexpected result: $value")
      }
    }

    "return CouldNotOrderMaterialsError" in new Fixture {
      val received = serviceUnderTest.constructShip(validOrderRequest.copy(guns = marketFailingGuns)).runSyncUnsafe()

      received shouldEqual Left(ShipConstructionError.CouldNotOrderMaterialsError("Market unavailable"))
    }

    "return CouldNotHireCrewError" in new Fixture {
      val received = serviceUnderTest.constructShip(validOrderRequest.copy(crew = crewFailingCrew)).runSyncUnsafe()

      received shouldEqual Left(ShipConstructionError.CouldNotHireCrewError("Crew unavailable"))
    }
  }
}

class ConstructShipLevelThreeTests extends BaseTestSuite {
  "ShipyardService#constructShip" should {
    "choose the faster market" in new Fixture {
      val mainScheduler     = TestScheduler()
      val blockingScheduler = TestScheduler()

      val serviceUnderTest2 = new ShipyardServiceImpl(
        new DelayedMarketService(prepareDelay = 3.second),
        new DelayedMarketService(prepareDelay = 2.second),
        crewService,
        shipConstructionOrderRepository,
        blockingScheduler
      )

      val statusF = serviceUnderTest2.constructShip(validOrderRequest).executeAsync.runToFuture(mainScheduler)

      mainScheduler.tick()
      statusF.value shouldBe None
      blockingScheduler.tick()
      mainScheduler.tick(2.second)

      statusF.value shouldBe Some(Success(Right(shipOrderId)))
    }

    // TODO: make the test more bulletproof
    "run crewService on a different thread pool" in new Fixture {
      val mainScheduler     = TestScheduler()
      val blockingScheduler = TestScheduler()

      val serviceUnderTest2 = new ShipyardServiceImpl(
        officialMarket,
        new DelayedMarketService(prepareDelay = 2.second),
        crewService,
        shipConstructionOrderRepository,
        blockingScheduler
      )

      val statusF = serviceUnderTest2.constructShip(validOrderRequest).executeAsync.runToFuture(mainScheduler)

      mainScheduler.tick()
      statusF.value shouldBe None
      blockingScheduler.tick()
      mainScheduler.tick()

      statusF.value shouldBe Some(Success(Right(shipOrderId)))
    }

    "call external services concurrently" in new Fixture {
      val mainScheduler     = TestScheduler()
      val blockingScheduler = TestScheduler()

      val serviceUnderTest2 = new ShipyardServiceImpl(
        new DelayedMarketService(confirmDelay = 4.second),
        new DelayedMarketService(prepareDelay = 4.second),
        new TestCrewService(Map(), mainScheduler, 5.second),
        shipConstructionOrderRepository,
        blockingScheduler
      )

      val statusF = serviceUnderTest2.constructShip(validOrderRequest).executeAsync.runToFuture(mainScheduler)
      mainScheduler.tick()
      blockingScheduler.tick()
      mainScheduler.tick()
      statusF.value shouldBe None

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
      statusF.value shouldBe Some(Success(Right(shipOrderId)))
    }

  }
}

object ConstructShipTests {
  private[domain] class Fixture {
    val shipOrderId       = OrderId("shipOrderId")
    val marketOrderId     = OrderId("marketOrderId")
    val crewOrderId       = OrderId("crewOrderId")
    val failingOrderId    = OrderId("failingOrderId")
    val marketFailingGuns = 27
    val crewFailingCrew   = 151

    object DummyMarketService extends MarketService {
      def prepareOrder(shipType: ShipType, guns: Int): Task[OrderId] =
        if (guns == marketFailingGuns) Task.now(failingOrderId)
        else Task.now(marketOrderId)

      def confirmOrder(orderId: OrderId): Task[Unit] =
        if (orderId == failingOrderId) Task.raiseError(new Exception(s"Market unavailable"))
        else Task.unit

      def checkOrderStatus(orderId: OrderId): Task[Option[MarketOrderStatus]] = ???
    }

    class DelayedMarketService(prepareDelay: FiniteDuration = 0.second, confirmDelay: FiniteDuration = 0.second) extends MarketService {
      def prepareOrder(shipType: ShipType, guns: Int): Task[OrderId] =
        Task.now(marketOrderId).delayExecution(prepareDelay)

      def confirmOrder(orderId: OrderId): Task[Unit] = Task.sleep(confirmDelay)

      def checkOrderStatus(orderId: OrderId): Task[Option[MarketOrderStatus]] = ???
    }

    object DummyCrewService extends CrewService {
      def hireCrew(size: Int): OrderId =
        if (size == crewFailingCrew) throw new Exception("Crew unavailable")
        else crewOrderId

      def checkCrewStatus(orderId: OrderId): Option[CrewOrderStatus] = ???
    }

    val validOrderRequest: ShipConstructionOrderRequest = ShipConstructionOrderRequest(
      shipType = ShipType.Frigate,
      crew = 150,
      guns = 26
    )

    val invalidOrderRequest: ShipConstructionOrderRequest = ShipConstructionOrderRequest(
      shipType = ShipType.Frigate,
      crew = 150,
      guns = 1
    )

    val order: ShipConstructionOrder = ShipConstructionOrder(
      marketType = MarketType.OfficialMarket,
      marketOrderId = marketOrderId,
      crewOrderId = crewOrderId,
      shipType = ShipType.Frigate,
      rate = Rate.SixthRate,
      crew = 150,
      guns = 26
    )

    object DummyConstructionOrderRepository extends ShipConstructionOrderRepository {
      def saveOrder(order: ShipConstructionOrder): Task[OrderId]           = Task.now(shipOrderId)
      def findOrder(orderId: OrderId): Task[Option[ShipConstructionOrder]] = ???
    }

    val officialMarket                  = DummyMarketService
    val smugglersMarket                 = DummyMarketService
    val crewService                     = DummyCrewService
    val shipConstructionOrderRepository = DummyConstructionOrderRepository

    val serviceUnderTest = new ShipyardServiceImpl(
      officialMarket,
      smugglersMarket,
      crewService,
      shipConstructionOrderRepository,
      Scheduler.io()
    )
  }
}
