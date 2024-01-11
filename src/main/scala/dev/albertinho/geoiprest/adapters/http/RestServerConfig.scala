package dev.albertinho.geoiprest.adapters.http
import cats.effect.Sync
import cats.implicits._
import com.comcast.ip4s.{Host, Port}
import pureconfig.error.CannotConvert
import pureconfig.generic.auto._
import pureconfig.module.catseffect.syntax._
import pureconfig.{ConfigReader, ConfigSource}
case class RestServerConfig(host: Host, port: Port)

object RestServerConfig {
  def make(
      host: String,
      port: Int
  ): Either[RuntimeException, RestServerConfig] = {

    val validHost = Host
      .fromString(host)
      .toValidNel(new RuntimeException(s"Invalid host: $host"))
    val validPort = Port
      .fromInt(port)
      .toValidNel(new RuntimeException(s"Invalid port: $port"))

    (validHost, validPort)
      .mapN(RestServerConfig.apply)
      .leftMap(_.toList.mkString(", "))
      .leftMap(new RuntimeException(_))
      .toEither
  }

  implicit val hostReader: ConfigReader[Host] = ConfigReader[String]
    .map(Host.fromString)
    .emap {
      case Some(host) => Right(host)
      case None       => Left(CannotConvert("", "Host", "Invalid host"))
    }
  implicit val portReader: ConfigReader[Port] = ConfigReader[Int].emap {
    case n if n >= 0 && n <= 65535 => Right(Port.fromInt(n).get)
    case n => Left(CannotConvert(n.toString, "Port", "Invalid port"))
  }
  def loadResource[F[_]: Sync]: F[RestServerConfig] =
    ConfigSource.default.loadF[F, RestServerConfig]()
}
