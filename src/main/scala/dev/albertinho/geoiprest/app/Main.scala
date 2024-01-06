package dev.albertinho.geoiprest.app

import cats.effect.{IO, IOApp}
import dev.albertinho.geoiprest.adapters.db.{GeoipCsvLoader, GeoipRepository, GeoipRepositoryBuilder}
import dev.albertinho.geoiprest.adapters.http.{RestServerConfig, RestServerFactory}
import dev.albertinho.geoiprest.domain.services.GeoipServiceImpl

object Main extends IOApp.Simple {
  def run: cats.effect.IO[Unit] = {
    val ioService = getDbFacade.map(GeoipServiceImpl.make[IO])
    val ioConfig = IO.fromEither(RestServerConfig.make("0.0.0.0", 8081))
    val res = for {
      service <- ioService
      config <- ioConfig
      server = RestServerFactory.make[IO](config, service)
    } yield server.allocated.map(_._2)

    res.foreverM
  }

  private def getDbFacade: IO[GeoipRepository[IO]] = {
    val loader = GeoipCsvLoader.fromFile[IO](
      "/Users/pitarch/code/sandboxes/geoiprest/data/dbip-city-ipv4.csv"
    )
    val builder = GeoipRepositoryBuilder.make[IO]
    val cleanedStream = loader.stream.map(_._2).collect {
      case scala.util.Success(value) => value
    }
    val facade = builder.build(cleanedStream)
    facade
  }
}
