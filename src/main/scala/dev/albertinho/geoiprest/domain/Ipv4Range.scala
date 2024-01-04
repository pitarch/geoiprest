package dev.albertinho.geoiprest.domain

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