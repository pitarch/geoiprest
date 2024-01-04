package dev.albertinho.geoiprest.infra.db

import dev.albertinho.geoiprest.domain.models.{IpRangeGeoInfo, Ipv4, Ipv4Range}

import scala.annotation.tailrec

object Ipv4LookupSupport {

  def find(ip: Ipv4, tree: IndexedSeq[IpRangeGeoInfo])(implicit
      ordering: Ordering[Ipv4Range]
  ): Option[IpRangeGeoInfo] = {
    val targetRange = Ipv4Range(ip, ip)

    @tailrec
    def go(left: Int, right: Int): Option[IpRangeGeoInfo] = {
      if (left > right) None
      else {
        val index = (left + right) / 2
        val mid = tree(index)
        val comparation = ordering.compare(targetRange, mid.ipRange)
        comparation match {
          case 0          => Some(mid)
          case x if x > 0 => go(index + 1, right)
          case _          => go(left, index - 1)
        }
      }
    }
    go(0, tree.length - 1)
  }
}
