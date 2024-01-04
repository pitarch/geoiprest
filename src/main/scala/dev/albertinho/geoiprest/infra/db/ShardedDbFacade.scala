package dev.albertinho.geoiprest.infra.db

import cats.effect.kernel.Sync
import cats.effect.std.MapRef
import cats.implicits._
import dev.albertinho.geoiprest.domain.models.{IpRangeGeoInfo, Ipv4}

object ShardedDbFacade {
  def make[F[_]: Sync](
      ref: MapRef[F, Int, Option[Vector[IpRangeGeoInfo]]]
  ): DbFacade[F] = (ip: Ipv4) => for {
    infos <- ref(ip.part1).get
    result <- Sync[F].delay(Ipv4LookupSupport.find(ip, infos.getOrElse(Vector.empty)))
  } yield result
}
