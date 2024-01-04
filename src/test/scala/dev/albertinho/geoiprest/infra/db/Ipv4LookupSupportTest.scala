package dev.albertinho.geoiprest.infra.db

import dev.albertinho.geoiprest.domain
import dev.albertinho.geoiprest.domain.{Ipv4, Ipv4Range}
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers

class Ipv4LookupSupportTest extends AsyncFlatSpec with Matchers {

  behavior of "ordering of Ipv4Range"

   it should "compare equally to itself" in {
     val ipStart = Ipv4.fromLong(0)
     val ipEnd = Ipv4.fromLong(255)
     val source = Ipv4Range(ipStart, ipEnd)
     domain.Ipv4Range.ordering.compare(source, source) shouldBe 0
     domain.Ipv4Range.ordering.compare(Ipv4Range(ipStart, ipStart), source) shouldBe 0
   }
}
