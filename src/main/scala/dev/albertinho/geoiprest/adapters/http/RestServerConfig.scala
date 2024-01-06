package dev.albertinho.geoiprest.adapters.http
import cats.implicits._
import com.comcast.ip4s.{Host, Port}
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
}
