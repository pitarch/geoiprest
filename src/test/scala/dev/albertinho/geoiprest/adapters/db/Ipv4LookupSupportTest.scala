package dev.albertinho.geoiprest.adapters.db

import dev.albertinho.geoiprest.domain.models.{Ipv4, Ipv4Range}
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers

class Ipv4LookupSupportTest extends AsyncFlatSpec with Matchers {

  behavior of "ordering of Ipv4Range"

   it should "compare equally to itself" in {
     val ipStart = Ipv4.fromLong(0)
     val ipEnd = Ipv4.fromLong(255)
     val source = Ipv4Range(ipStart, ipEnd)
     Ipv4Range.ordering.compare(source, source) shouldBe 0
     Ipv4Range.ordering.compare(Ipv4Range(ipStart, ipStart), source) shouldBe 0
   }
}
