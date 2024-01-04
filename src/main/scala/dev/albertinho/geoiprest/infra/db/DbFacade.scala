package dev.albertinho.geoiprest.infra.db

import cats.effect.kernel.Sync
import dev.albertinho.geoiprest.domain.models.{IpRangeGeoInfo, Ipv4}

trait DbFacade[F[_]] {

  def find(ip: Ipv4): F[Option[IpRangeGeoInfo]]
}

object DbFacade {

  def make[F[_]: Sync](db: IndexedSeq[IpRangeGeoInfo]): DbFacade[F] =
    (ip: Ipv4) => Sync[F].delay(Ipv4LookupSupport.find(ip, db))

}
