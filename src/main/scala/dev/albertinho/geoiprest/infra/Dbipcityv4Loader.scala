package dev.albertinho.geoiprest.infra

import dev.albertinho.geoiprest.domain
import fs2.io.file.Files
import dev.albertinho.geoiprest.parsing
import scala.util.Try

trait Dbipcityv4Loader[F[_]] {

  def stream: fs2.Stream[F, (Long, Try[domain.IpRangeGeoInfo])]
}

object Dbipcityv4Loader {

  import cats.implicits._

  def fromFile[F[_]: Files](file: String): Dbipcityv4Loader[F] =
    new Dbipcityv4Loader[F] {
      override def stream: fs2.Stream[F, (Long, Try[domain.IpRangeGeoInfo])] =
        fs2.io.file
          .Files[F]
          .readUtf8Lines(fs2.io.file.Path(file))
          .through(parseLine)
    }

  def fromString[F[_]](content: String): Dbipcityv4Loader[F] =
    new Dbipcityv4Loader[F] {
      override def stream: fs2.Stream[F, (Long, Try[domain.IpRangeGeoInfo])] =
        fs2.Stream
          .emits(content.split("\n"))
          .through(parseLine)
    }

  private def parseLine[F[_]](
      input: fs2.Stream[F, String]
  ): fs2.Stream[F, (Long, Try[domain.IpRangeGeoInfo])] =
    input
      .map { _.trim() }
      .zipWithIndex
      .filter { case (line, _) => line.nonEmpty }
      .map { case (line, index) => (index, parsing.parseLineUnsafe(line)) }
}
