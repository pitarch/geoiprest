package dev.albertinho.geoiprest

object domain {

  type Ipv4Address = (Int, Int, Int, Int)

  final case class Ipv4 private (address: Ipv4Address) {
    def toLong: Long =
      (part1.toLong << 24) + (part2.toLong << 16) + (part3.toLong << 8) + part4.toLong

    def part1: Int = address._1
    def part2: Int = address._2
    def part3: Int = address._3
    def part4: Int = address._4
    override def toString: String = s"${part1}.${part2}.${part3}.${part4}"
  }
  object Ipv4 {
    def apply(raw: String): Ipv4 = {
      val parts = raw.trim.split('.')
      if (parts.length != 4)
        throw new IllegalArgumentException(s"Invalid IPv4 address: $raw")
      val bytes = parts.map(_.toInt)
      Ipv4((bytes(0), bytes(1), bytes(2), bytes(3)))
    }
  }

  final case class Ipv4Range(start: Ipv4, end: Ipv4) {

    def contains(ip: Ipv4): Boolean =
      ip.toLong >= start.toLong && ip.toLong <= end.toLong

    def isSubset(other: Ipv4Range): Boolean =
      contains(other.start) && contains(other.end)
  }

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
}
