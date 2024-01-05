package dev.albertinho.geoiprest.ports

import dev.albertinho.geoiprest.domain.models.IpRangeGeoInfo

trait GeoipService[F[_]] {

  def getInfo(ip: String): F[Option[IpRangeGeoInfo]]
}
