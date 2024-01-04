package dev.albertinho.geoiprest.domain.models

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class Ipv4Test extends AnyFlatSpec with Matchers {

  behavior of "Ipv4"

  it should "be created from a valid string" in {
    val ipv4 = Ipv4("1.2.3.4")
    ipv4.part1 shouldBe 1
    ipv4.part2 shouldBe 2
    ipv4.part3 shouldBe 3
    ipv4.part4 shouldBe 4
  }

  it should "convert ip from/to long" in {
    Ipv4("0.0.0.1").toLong shouldBe 1
    Ipv4.fromLong(1).toLong shouldBe 1
    Ipv4("0.0.1.0").toLong shouldBe 256
    Ipv4.fromLong(256).toLong shouldBe 256
    Ipv4("0.1.0.0").toLong shouldBe 65536
    Ipv4.fromLong(65536).toLong shouldBe 65536
  }
}
