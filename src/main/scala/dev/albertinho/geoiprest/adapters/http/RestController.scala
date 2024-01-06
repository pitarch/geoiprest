package dev.albertinho.geoiprest.adapters.http

import cats.effect.Async
import cats.implicits._
import dev.albertinho.geoiprest.domain.models.{IpRangeGeoInfo, Ipv4}
import dev.albertinho.geoiprest.ports.GeoipService
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.dsl.Http4sDsl
import org.http4s.{HttpRoutes, Response}

class RestController[F[_]: Async](service: GeoipService[F])
    extends Http4sDsl[F] {

  val routes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "geoip" / ip => getInfo(ip)
  }

  private def getInfo(rawIp: String): F[Response[F]] = {

    val doGetRestInfo: String => F[Response[F]] = service.getInfo(_).flatMap {
      case Some(info) => Ok(info)
      case None       => NotFound()
    }
    Async[F].pure(rawIp).map(Ipv4.apply).attempt.flatMap {
      case Right(_) => doGetRestInfo(rawIp)
      case Left(_)  => BadRequest(s"Malformed ip: $rawIp")
    }
  }

  // create an implicit circe encoder for IpRangeGeoInfo
  implicit val encoderIpRangeGeoInfo: io.circe.Encoder[IpRangeGeoInfo] =
    io.circe.Encoder.forProduct7(
      "countryCode",
      "stateProv",
      "city",
      "latitude",
      "longitude",
      "startAddress",
      "endAddress"
    )(info =>
      (
        info.countryCode,
        info.stateProv,
        info.city,
        info.latitude,
        info.longitude,
        info.ipRange.start.toString(),
        info.ipRange.end.toString()
      )
    )
}

object RestController {
  def make[F[_]: Async](service: GeoipService[F]): RestController[F] =
    new RestController[F](service)
}
