package dev.albertinho.geoiprest

import scala.util.Try
import domain.IpRangeGeoInfo
import cats.ApplicativeError
object parsing {

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
      domain.IpRangeGeoInfo(
        ipRange = domain.Ipv4Range(
          start = domain.Ipv4(lineSplit(0)),
          end = domain.Ipv4(lineSplit(1))
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
