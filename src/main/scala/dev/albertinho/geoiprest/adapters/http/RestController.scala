package dev.albertinho.geoiprest.adapters.http

import cats.effect.Async
import cats.implicits._
import dev.albertinho.geoiprest.ports.GeoipService
import org.http4s.dsl.Http4sDsl
import org.http4s.{HttpRoutes, Response}
import io.circe.generic.auto._
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder

class RestController[F[_]: Async](service: GeoipService[F])
    extends Http4sDsl[F] {

  val routes: HttpRoutes[F] = HttpRoutes.of[F] { case GET -> Root / "geoip" / ip => getInfo(ip) }

  private def getInfo(ip: String): F[Response[F]] =
    service.getInfo(ip).flatMap {
      case Some(info) => Ok(info)
      case None       => NotFound()
    }
}
