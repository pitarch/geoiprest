package dev.albertinho.geoiprest.domain


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