package dev.albertinho.geoiprest.adapters.db

import cats.implicits._
import dev.albertinho.geoiprest.domain.models.IpRangeGeoInfo
import fs2.io.file.Files
import org.typelevel.log4cats.Logger

import scala.util.Try

trait GeoipCsvLoader[F[_]] {

  def stream: fs2.Stream[F, (Long, Try[IpRangeGeoInfo])]
}

object GeoipCsvLoader {

  def fromFile[F[_]: Files](
      file: String
  )(implicit logger: Logger[F]): GeoipCsvLoader[F] =
    new GeoipCsvLoader[F] {
      override def stream: fs2.Stream[F, (Long, Try[IpRangeGeoInfo])] = {
        // log file name and the stream the content
        fs2.Stream.eval(logger.info(s"Loading file $file")) >>
          fs2.io.file
            .Files[F]
            .readUtf8Lines(fs2.io.file.Path(file))
            .through(parseLine)
      }
    }

  private def parseLine[F[_]](
      input: fs2.Stream[F, String]
  ): fs2.Stream[F, (Long, Try[IpRangeGeoInfo])] =
    input
      .map { _.trim() }
      .zipWithIndex
      .filter { case (line, _) => line.nonEmpty }
      .map { case (line, index) =>
        (index, GeoipCsvEntryParser.parseLineUnsafe(line))
      }

  def fromString[F[_]](content: String): GeoipCsvLoader[F] =
    new GeoipCsvLoader[F] {
      override def stream: fs2.Stream[F, (Long, Try[IpRangeGeoInfo])] =
        fs2.Stream
          .emits(content.split("\n"))
          .through(parseLine)
    }
}
