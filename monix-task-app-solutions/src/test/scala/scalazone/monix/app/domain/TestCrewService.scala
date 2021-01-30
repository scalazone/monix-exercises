package scalazone.monix.app.domain

import monix.execution.Scheduler
import scalazone.monix.app.OrderId
import scalazone.monix.app.external.{CrewOrderStatus, CrewService}

import java.util.UUID
import java.util.concurrent.atomic.AtomicReference
import scala.concurrent.duration.FiniteDuration

final class TestCrewService(
    initialOrders: Map[OrderId, CrewOrderStatus] = Map.empty,
    scheduler: Scheduler,
    delay: FiniteDuration
) extends CrewService {
  private val storage: AtomicReference[Map[OrderId, CrewOrderStatus]] = new AtomicReference(initialOrders)

  override def hireCrew(size: Int): OrderId = {
    val id = OrderId(UUID.randomUUID().toString)
    // We can use this to check if the task was scheduled
    scheduler.scheduleOnce(delay)(())
    storage.updateAndGet(_.updated(id, CrewOrderStatus(0, size)))
    id
  }

  override def checkCrewStatus(orderId: OrderId): Option[CrewOrderStatus] = {
    // We can use this to check if the task was scheduled
    scheduler.scheduleOnce(delay)(())
    storage.get().get(orderId)
  }
}
