package dev.albertinho.geoiprest.domain.models

final case class IpRangeGeoInfo(
    ipRange: Ipv4Range,
    countryCode: String,
    stateProv: String,
    city: String,
    latitude: Double,
    longitude: Double
) {

  def contains(ip: Ipv4): Boolean =
    ip.toLong >= ipRange.start.toLong && ip.toLong <= ipRange.end.toLong
}
