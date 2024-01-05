package dev.albertinho.geoiprest.adapters.http

import cats.effect.Async
import cats.implicits._
import dev.albertinho.geoiprest.domain.models.Ipv4
import dev.albertinho.geoiprest.ports.GeoipService
import io.circe.generic.auto._
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
      case Left(_)  => BadRequest()
    }

  }
}
