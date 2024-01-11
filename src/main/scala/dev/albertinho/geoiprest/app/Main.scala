package dev.albertinho.geoiprest.app

import cats.effect.{IO, IOApp}
import dev.albertinho.geoiprest.adapters.db.{GeoipCsvLoader, GeoipRepository, GeoipRepositoryBuilder}
import dev.albertinho.geoiprest.adapters.http.{RestServerConfig, RestServerFactory}
import dev.albertinho.geoiprest.domain.services.GeoipServiceImpl
import org.typelevel.log4cats.slf4j.Slf4jLogger

object Main extends IOApp.Simple {
  def run: cats.effect.IO[Unit] =
    for {
      config <- RestServerConfig.loadResource[IO]
        //IO.fromEither(RestServerConfig.make("0.0.0.0", 8081))
      service <- getDbFacade.map(GeoipServiceImpl.make[IO])
      server <- RestServerFactory
        .make[IO](config, service)
        .use(_ => IO.never)
        .void
    } yield server

  private def getDbFacade: IO[GeoipRepository[IO]] = {
    implicit val logger = Slf4jLogger.getLogger[IO]
    val loader = GeoipCsvLoader.fromFile[IO](
      "/Users/pitarch/code/sandboxes/geoiprest/data/dbip-city-ipv4.csv"
    )
    val builder = GeoipRepositoryBuilder.make[IO]
    val cleanedStream = loader.stream.map(_._2).collect {
      case scala.util.Success(value) => value
    }
    for {
      logger <- Slf4jLogger.create[IO]
      _ <- logger.info("Loading database...")
      repo <- builder.build(cleanedStream)
      _ <- logger.info("Database loaded")
    } yield repo
  }
}
