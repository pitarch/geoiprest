package dev.albertinho.geoiprest.domain.services

import cats.MonadError
import cats.effect.kernel.Sync
import cats.implicits._
import dev.albertinho.geoiprest.adapters.db.GeoipRepository
import dev.albertinho.geoiprest.domain.models.{IpRangeGeoInfo, Ipv4}
import dev.albertinho.geoiprest.ports.GeoipService

class GeoipServiceImpl[F[_]: Sync](repo: GeoipRepository[F]) extends GeoipService[F]{
  override def getInfo(ip: String): F[Option[IpRangeGeoInfo]] = MonadError[F, Throwable]
    .catchNonFatal { Ipv4(ip) }
    .flatMap(repo.find)
}

object GeoipServiceImpl {
  def make[F[_]: Sync](repo: GeoipRepository[F]): GeoipService[F] = new GeoipServiceImpl[F](repo)
}
