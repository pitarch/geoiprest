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

    implicit val ordering: Ordering[Ipv4] =
      Ordering.by(ipv4 => (ipv4.part1, ipv4.part2, ipv4.part3, ipv4.part4))

    def fromLong(ip: Long): Ipv4 = {
      val part1 = (ip >> 24) & 0xFF
      val part2 = (ip >> 16) & 0xFF
      val part3 = (ip >> 8) & 0xFF
      val part4 = ip & 0xFF
      Ipv4((part1.toInt, part2.toInt, part3.toInt, part4.toInt))
    }
  }

  final case class Ipv4Range(start: Ipv4, end: Ipv4) {

    def contains(ip: Ipv4): Boolean =
      ip.toLong >= start.toLong && ip.toLong <= end.toLong

    def isSubset(other: Ipv4Range): Boolean =
      contains(other.start) && contains(other.end)
  }

  object Ipv4Range {

    implicit val ordering: Ordering[Ipv4Range] = (x: Ipv4Range, y: Ipv4Range) => if (x.start.toLong < y.start.toLong) -1
    else if (x.end.toLong > y.end.toLong) 1
    else 0
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
