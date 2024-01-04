package dev.albertinho.geoiprest.infra.db

import scala.util.Try
import cats.ApplicativeError
import dev.albertinho.geoiprest.domain.models.{IpRangeGeoInfo, Ipv4, Ipv4Range}

trait Parser[F[_]] {
  def parse(line: String): F[IpRangeGeoInfo]
}

object Parser {

  def parseLine[F[_]](
      line: String
  )(implicit F: ApplicativeError[F, Throwable]): F[IpRangeGeoInfo] = {

    parseLineUnsafe(line) match {
      case scala.util.Success(value)     => F.pure(value)
      case scala.util.Failure(exception) => F.raiseError(exception)
    }

  }

  def parseLineUnsafe(line: String): Try[IpRangeGeoInfo] = {
    val lineSplit = line.split(",")
    Try {
      IpRangeGeoInfo(
        ipRange = Ipv4Range(
          start = Ipv4(lineSplit(0)),
          end = Ipv4(lineSplit(1))
        ),
        countryCode = lineSplit(2),
        stateProv = lineSplit(3),
        city = lineSplit(5),
        latitude = lineSplit(7).toDouble,
        longitude = lineSplit(8).toDouble
      )
    }
  }

}
