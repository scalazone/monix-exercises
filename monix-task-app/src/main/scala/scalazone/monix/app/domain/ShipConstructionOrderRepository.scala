package scalazone.monix.app.domain

import monix.eval.Task
import scalazone.monix.app._

import java.util.UUID
import java.util.concurrent.atomic.AtomicReference

trait ShipConstructionOrderRepository {
  def saveOrder(order: ShipConstructionOrder): Task[OrderId]
  def findOrder(orderId: OrderId): Task[Option[ShipConstructionOrder]]
}

final class InMemShipConstructionOrderRepository(initialOrders: Map[OrderId, ShipConstructionOrder] = Map.empty)
    extends ShipConstructionOrderRepository {
  private val storage: AtomicReference[Map[OrderId, ShipConstructionOrder]] = new AtomicReference(initialOrders)

  override def saveOrder(order: ShipConstructionOrder): Task[OrderId] = Task {
    val id = OrderId(UUID.randomUUID().toString)
    storage.updateAndGet(_.updated(id, order))
    id
  }

  override def findOrder(orderId: OrderId): Task[Option[ShipConstructionOrder]] = Task {
    storage.get().get(orderId)
  }
}

final case class ShipConstructionOrder(
    marketType: MarketType,
    marketOrderId: OrderId,
    crewOrderId: OrderId,
    shipType: ShipType,
    rate: Rate,
    crew: Int,
    guns: Int
)
