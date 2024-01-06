package dev.albertinho.geoiprest.adapters.db

import cats.effect.kernel.Sync
import cats.effect.std.MapRef
import cats.implicits._
import dev.albertinho.geoiprest.domain.models.IpRangeGeoInfo

trait GeoipRepositoryBuilder[F[_]] {

  def build(stream: fs2.Stream[F, IpRangeGeoInfo]): F[GeoipRepository[F]]
}

object GeoipRepositoryBuilder {

  def make[F[_]: Sync]: GeoipRepositoryBuilder[F] = new ShardedBuilder[F]

  class ShardedBuilder[F[_]: Sync] extends GeoipRepositoryBuilder[F] {
    override def build(
        stream: fs2.Stream[F, IpRangeGeoInfo]
    ): F[GeoipRepository[F]] = {
      MapRef
        .ofConcurrentHashMap[F, Int, Vector[IpRangeGeoInfo]]()
        .flatMap { ref =>
          buildWithRef(stream, ref) *> Sync[F].delay(
            ShardedGeoipRepository.make[F](ref)
          )
        }
    }

    private def buildWithRef(
        stream: fs2.Stream[F, IpRangeGeoInfo],
        ref: MapRef[F, Int, Option[Vector[IpRangeGeoInfo]]]
    ): F[Unit] = {
      stream
        .groupAdjacentBy(_.ipRange.start.part1)
        .map { case (shard, chunk) =>
          (shard, chunk.toVector.sortBy(_.ipRange.start))
        }
        .evalTap { case (shard, geoInfos) => ref(shard).set(geoInfos.some) }
        .compile
        .drain
    }
  }
}
