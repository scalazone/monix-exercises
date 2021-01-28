package scalazone.monix.app.external

import monix.eval.Task
import scalazone.monix.app.{OrderId, ShipType}

import java.util.UUID
import java.util.concurrent.atomic.AtomicReference

trait MarketService {
  def prepareOrder(shipType: ShipType, guns: Int): Task[OrderId]

  def confirmOrder(orderId: OrderId): Task[Unit]

  def checkOrderStatus(orderId: OrderId): Task[Option[MarketOrderStatus]]
}

final class InMemMarketService extends MarketService {
  private val storage: AtomicReference[Map[OrderId, MarketOrderStatus]] = new AtomicReference(Map.empty)

  override def prepareOrder(shipType: ShipType, guns: Int): Task[OrderId] = Task {
    val id = OrderId(UUID.randomUUID().toString)
    storage.updateAndGet(_.updated(id, MarketOrderStatus(0)))
    id
  }

  override def confirmOrder(orderId: OrderId): Task[Unit] = Task.unit

  override def checkOrderStatus(orderId: OrderId): Task[Option[MarketOrderStatus]] = Task {
    storage.get().get(orderId)
  }
}

final case class MarketOrderStatus(gunsReady: Int)
