package scalazone.monix.app

import RatingValidationError._

enum Rate:
  case Unrated, SixthRate, FifthRate, FourthRate, ThirdRate, SecondRate, FirstRate

enum RatingValidationError(val msg: String):
  case InvalidCrewSize(received: Int, expectedMin: Int, expectedMax: Int) extends RatingValidationError(s"Invalid crew size = $received, expected a value between $expectedMin and $expectedMax")
  case InvalidGunCount(received: Int, expectedMin: Int, expectedMax: Int) extends RatingValidationError(s"Invalid gun count = $received, expected a value between $expectedMin and $expectedMax")
  case RatingError(override val msg: String) extends RatingValidationError(msg)

object Rate {
  // https://en.wikipedia.org/wiki/Rating_system_of_the_Royal_Navy
  def fromOrder(order: ShipConstructionOrderRequest): Either[RatingValidationError, Rate] =
    order.shipType match {
      case ShipType.Sloop => rateSloop(order.crew, order.guns)
      case ShipType.Frigate => rateFrigate(order.crew, order.guns)
      case ShipType.ShipOfTheLine => rateShipOfTheLine(order.crew, order.guns)
    }

  private def rateSloop(crew: Int, guns: Int): Either[RatingValidationError, Rate] =
    if (crew >= 90 && crew <= 125) {
      if (guns >= 16 && guns <= 18) Right(Rate.Unrated)
      else Left(InvalidGunCount(guns, 16, 18))
    } else Left(InvalidCrewSize(crew, 90, 125))

  private def rateFrigate(crew: Int, guns: Int): Either[RatingValidationError, Rate] = {
    val isSixthRateFrigate: Either[RatingValidationError, Rate] =
      if (crew >= 140 && crew <= 200) {
        if (guns >= 20 && guns <= 28) Right(Rate.SixthRate)
        else Left(InvalidGunCount(guns, 20, 28))
      } else Left(InvalidCrewSize(crew, 140, 200))

    val isFifthRateFrigate: Either[RatingValidationError, Rate] =
      if (crew >= 200 && crew <= 300) {
        if (guns >= 32 && guns <= 44) Right(Rate.FifthRate)
        else Left(InvalidGunCount(guns, 32, 44))
      } else Left(InvalidCrewSize(crew, 200, 300))

    isSixthRateFrigate.left.flatMap { err1 =>
      isFifthRateFrigate.left.map(err2 => RatingError(s"${err1.msg} OR ${err2.msg}"))
    }
  }

  private def rateShipOfTheLine(crew: Int, guns: Int): Either[RatingValidationError, Rate] = {
    val isFirstRate: Either[RatingValidationError, Rate] =
      if (crew >= 850 && crew <= 875) {
        if (guns >= 100 && guns <= 112) Right(Rate.FirstRate)
        else Left(InvalidGunCount(guns, 100, 112))
      } else Left(InvalidCrewSize(crew, 850, 875))

    val isSecondRate: Either[RatingValidationError, Rate] =
      if (crew >= 700 && crew <= 750) {
        if (guns >= 80 && guns <= 98) Right(Rate.FifthRate)
        else Left(InvalidGunCount(guns, 80, 98))
      } else Left(InvalidCrewSize(crew, 700, 750))

    val isThirdRate: Either[RatingValidationError, Rate] =
      if (crew >= 500 && crew <= 650) {
        if (guns >= 64 && guns <= 80) Right(Rate.FifthRate)
        else Left(InvalidGunCount(guns, 64, 80))
      } else Left(InvalidCrewSize(crew, 500, 650))

    val isFourthRate: Either[RatingValidationError, Rate] =
      if (crew >= 320 && crew <= 420) {
        if (guns >= 50 && guns <= 60) Right(Rate.FifthRate)
        else Left(InvalidGunCount(guns, 50, 60))
      } else Left(InvalidCrewSize(crew, 320, 420))

    isFirstRate.left.flatMap { err1 =>
      isSecondRate.left.flatMap { err2 =>
        isThirdRate.left.flatMap { err3 =>
          isFourthRate.left.map { err4 =>
            RatingError(s"${err1.msg} OR ${err2.msg} OR ${err3.msg} OR ${err4.msg}")
          }
        }
      }
    }
  }
}
