package scalazone.monix.app.external

import java.util.UUID
import scalazone.monix.app.OrderId

import java.util.concurrent.atomic.AtomicReference

trait CrewService {
  def hireCrew(size: Int): OrderId

  def checkCrewStatus(orderId: OrderId): Option[CrewOrderStatus]
}

final case class CrewOrderStatus(crewReady: Int, crewOrdered: Int)

final class InMemCrewService(initialOrders: Map[OrderId, CrewOrderStatus] = Map.empty) extends CrewService {
  private val storage: AtomicReference[Map[OrderId, CrewOrderStatus]] = new AtomicReference(initialOrders)

  override def hireCrew(size: Int): OrderId = {
    val id = OrderId(UUID.randomUUID().toString)
    storage.updateAndGet(_.updated(id, CrewOrderStatus(0, size)))
    id
  }

  override def checkCrewStatus(orderId: OrderId): Option[CrewOrderStatus] =
    storage.get().get(orderId)
}
