package dev.albertinho.geoiprest.adapters.db

import dev.albertinho.geoiprest.domain.models.IpRangeGeoInfo
import fs2.io.file.Files

import scala.util.Try

trait Loader[F[_]] {

  def stream: fs2.Stream[F, (Long, Try[IpRangeGeoInfo])]
}

object Loader {

  def fromFile[F[_]: Files](file: String): Loader[F] =
    new Loader[F] {
      override def stream: fs2.Stream[F, (Long, Try[IpRangeGeoInfo])] =
        fs2.io.file
          .Files[F]
          .readUtf8Lines(fs2.io.file.Path(file))
          .through(parseLine)
    }

  def fromString[F[_]](content: String): Loader[F] =
    new Loader[F] {
      override def stream: fs2.Stream[F, (Long, Try[IpRangeGeoInfo])] =
        fs2.Stream
          .emits(content.split("\n"))
          .through(parseLine)
    }

  private def parseLine[F[_]](
      input: fs2.Stream[F, String]
  ): fs2.Stream[F, (Long, Try[IpRangeGeoInfo])] =
    input
      .map { _.trim() }
      .zipWithIndex
      .filter { case (line, _) => line.nonEmpty }
      .map { case (line, index) => (index, Parser.parseLineUnsafe(line)) }
}
