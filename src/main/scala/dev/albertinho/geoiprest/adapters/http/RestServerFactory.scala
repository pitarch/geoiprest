package dev.albertinho.geoiprest.adapters.http

import cats.effect.Async
import cats.effect.kernel.Resource
import dev.albertinho.geoiprest.ports.GeoipService
import fs2.io.net.Network
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Server

object RestServerFactory {

  def make[F[_]: Async: Network](
      config: RestServerConfig,
      service: GeoipService[F]
  ): Resource[F, Server] =
    EmberServerBuilder
      .default[F]
      .withHttpApp(new RestController[F](service).routes.orNotFound)
      .withHost(config.host)
      .withPort(config.port)
      .build

}
