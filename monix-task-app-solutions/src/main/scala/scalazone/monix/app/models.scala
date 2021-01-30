package scalazone.monix.app

import sttp.tapir.Codec.PlainCodec
import sttp.tapir.{Codec, Schema, SchemaType}
import io.circe.syntax._
import io.circe._

final case class OrderId(id: String) extends AnyVal

object OrderId {
  implicit val orderIdCodec: PlainCodec[OrderId] = Codec.string.map(OrderId(_))(_.id)
  implicit val schema: Schema[OrderId] = Schema(SchemaType.SString)

  implicit val encoder: Encoder[OrderId] = new Encoder[OrderId] {
    final def apply(a: OrderId): Json = Json.obj(
      ("id", Json.fromString(a.id))
    )
  }

  implicit val decoder: Decoder[OrderId] = new Decoder[OrderId] {
    final def apply(c: HCursor): Decoder.Result[OrderId] =
      for {
        id <- c.downField("id").as[String]
      } yield {
        new OrderId(id)
      }
  }
}

final case class ApiError(errorMsg: String)

object ApiError {
  implicit val schema: Schema[ApiError] = Schema(SchemaType.SString)

  implicit val encoder: Encoder[ApiError] = new Encoder[ApiError] {
    final def apply(a: ApiError): Json = Json.obj(
      ("errorMsg", Json.fromString(a.errorMsg))
    )
  }

  implicit val decoder: Decoder[ApiError] = new Decoder[ApiError] {
    final def apply(c: HCursor): Decoder.Result[ApiError] =
      for {
        errorMsg <- c.downField("errorMsg").as[String]
      } yield ApiError(errorMsg)
  }
}

enum ShipType:
  case Sloop, Frigate, ShipOfTheLine

final case class ShipConstructionOrderRequest(
    shipType: ShipType,
    crew: Int,
    guns: Int
)

object ShipConstructionOrderRequest {
  implicit val schema: Schema[ShipConstructionOrderRequest] = Schema(SchemaType.SString)
  
  implicit val encoder: Encoder[ShipConstructionOrderRequest] = new Encoder[ShipConstructionOrderRequest] {
    final def apply(a: ShipConstructionOrderRequest): Json = Json.obj(
      ("shipType", Json.fromString(a.shipType.toString)),
      ("crew", Json.fromInt(a.crew)),
      ("guns", Json.fromInt(a.guns))
    )
  }

  implicit val decodeFoo: Decoder[ShipConstructionOrderRequest] = new Decoder[ShipConstructionOrderRequest] {
    final def apply(c: HCursor): Decoder.Result[ShipConstructionOrderRequest] =
      for {
        shipTypeStr <- c.downField("shipType").as[String]
        shipType <- {
          shipTypeStr match {
          case "Sloop" => Right(ShipType.Sloop)
          case "Frigate" => Right(ShipType.Frigate)
          case "ShipOfTheLine" => Right(ShipType.ShipOfTheLine)
          case _ => Left(DecodingFailure.apply(s"Invalid shipType: $shipTypeStr", List()))
        }
        }
        crew <- c.downField("crew").as[Int]
        guns <- c.downField("guns").as[Int]

      } yield {
        new ShipConstructionOrderRequest(shipType, crew, guns)
      }
  }
}

final case class ShipConstructionStatus(
    orderId: OrderId,
    shipType: ShipType,
    rate: Rate,
    crewReady: Int,
    crewTotal: Int,
    gunsReady: Int,
    gunsTotal: Int
)

object ShipConstructionStatus {
  implicit val schema: Schema[ShipConstructionStatus] = Schema(SchemaType.SString)

  implicit val encoder: Encoder[ShipConstructionStatus] = new Encoder[ShipConstructionStatus] {
    final def apply(a: ShipConstructionStatus): Json = Json.obj(
      ("orderId", Json.fromString(a.orderId.id)),
      ("shipType", Json.fromString(a.shipType.toString)),
      ("rate", Json.fromString(a.rate.toString)),
      ("crewReady", Json.fromInt(a.crewReady)),
      ("crewTotal", Json.fromInt(a.crewTotal)),
      ("gunsReady", Json.fromInt(a.gunsReady)),
      ("gunsTotal", Json.fromInt(a.gunsTotal))
    )
  }

  implicit val decoder: Decoder[ShipConstructionStatus] = new Decoder[ShipConstructionStatus] {
    final def apply(c: HCursor): Decoder.Result[ShipConstructionStatus] =
      for {
        orderId <- c.downField("orderId").as[String]
        shipTypeStr <- c.downField("shipType").as[String]
        shipType <- {
          shipTypeStr match {
            case "Sloop" => Right(ShipType.Sloop)
            case "Frigate" => Right(ShipType.Frigate)
            case "ShipOfTheLine" => Right(ShipType.ShipOfTheLine)
            case _ => Left(DecodingFailure.apply(s"Invalid shipType: $shipTypeStr", List()))
          }
        }
        rateStr <- c.downField("rate").as[String]
        rate <- {
          rateStr match {
            case "Unrated" => Right(Rate.Unrated)
            case "SixthRate" => Right(Rate.SixthRate)
            case "FifthRate" => Right(Rate.FifthRate)
            case "FourthRate" => Right(Rate.FourthRate)
            case "ThirdRate" => Right(Rate.ThirdRate)
            case "SecondRate" => Right(Rate.SecondRate)
            case "FirstRate" => Right(Rate.FirstRate)
            case _ => Left(DecodingFailure.apply(s"Invalid rate: $rateStr", List()))
          }
        }
        crewReady <- c.downField("crewReady").as[Int]
        crewTotal <- c.downField("crewTotal").as[Int]

        gunsReady <- c.downField("gunsReady").as[Int]
        gunsTotal <- c.downField("gunsTotal").as[Int]

      } yield {
        new ShipConstructionStatus(
          OrderId(orderId),
          shipType,
          rate,
          crewReady,
          crewTotal,
          gunsReady,
          gunsTotal
        )
      }
  }
}

enum ShipGetStatusError:
  case ShipOrderNotFound(orderId: OrderId) extends ShipGetStatusError
  case CrewOrderNotFound(crewOrderId: OrderId) extends ShipGetStatusError
  case MarketOrderNotFound(marketOrderId: OrderId) extends ShipGetStatusError
  case ExternalOrdersNotFound(marketOrderId: OrderId, crewOrderId: OrderId) extends ShipGetStatusError
  case ExternalServicesFailure(msg: String) extends ShipGetStatusError

enum ShipConstructionError:
  case ValidationError(msg: String) extends ShipConstructionError
  case CouldNotOrderMaterialsError(msg: String) extends ShipConstructionError
  case CouldNotHireCrewError(msg: String) extends ShipConstructionError

enum MarketType:
   case OfficialMarket, SmugglersMarket