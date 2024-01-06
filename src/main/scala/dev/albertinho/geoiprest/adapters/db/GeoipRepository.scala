package dev.albertinho.geoiprest.adapters.db

import cats.effect.kernel.Sync
import dev.albertinho.geoiprest.domain.models.{IpRangeGeoInfo, Ipv4}

trait GeoipRepository[F[_]] {

  def find(ip: Ipv4): F[Option[IpRangeGeoInfo]]
}

object GeoipRepository {

  def make[F[_]: Sync](db: IndexedSeq[IpRangeGeoInfo]): GeoipRepository[F] =
    (ip: Ipv4) => Sync[F].delay(Ipv4LookupSupport.find(ip, db))

}
