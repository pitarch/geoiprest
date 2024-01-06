package dev.albertinho.geoiprest.adapters.db

import cats.effect.kernel.Sync
import cats.effect.std.MapRef
import cats.implicits._
import dev.albertinho.geoiprest.domain.models.IpRangeGeoInfo
import org.typelevel.log4cats.Logger

trait GeoipRepositoryBuilder[F[_]] {

  def build(stream: fs2.Stream[F, IpRangeGeoInfo]): F[GeoipRepository[F]]
}

object GeoipRepositoryBuilder {

  def make[F[_]: Sync]: GeoipRepositoryBuilder[F] = new ShardedBuilder[F]

  class ShardedBuilder[F[_]: Sync] extends GeoipRepositoryBuilder[F] {
    override def build(
        stream: fs2.Stream[F, IpRangeGeoInfo]
    ): F[GeoipRepository[F]] = {
      val mapRef = MapRef.ofConcurrentHashMap[F, Int, Vector[IpRangeGeoInfo]]()
      val logger = org.typelevel.log4cats.slf4j.Slf4jLogger.getLogger[F]
      for {
        ref <- mapRef
        _ <- buildWithRef(stream, ref)(logger)
        // foo <- ref.traverseCollect { case Some(geoipInfos) => Sync[F].delay(geoipInfos.size) }
        _ <- logger.info(s"Sharded database built")
      } yield ShardedGeoipRepository.make[F](ref)
    }

    private def buildWithRef(
        stream: fs2.Stream[F, IpRangeGeoInfo],
        ref: MapRef[F, Int, Option[Vector[IpRangeGeoInfo]]]
    )(implicit logger: Logger[F]): F[Unit] = {
      stream
        .groupAdjacentBy(_.ipRange.start.part1)
        .evalTap { case (shardId, chunk) =>
          logger.info(s"Building shard $shardId with ${chunk.size} elements")
        }
        .map { case (shard, chunk) =>
          (shard, chunk.toVector.sortBy(_.ipRange.start))
        }
        .evalTap { case (shard, geoInfos) => ref(shard).set(geoInfos.some) }
        .compile
        .drain
    }
  }
}
