package dev.albertinho.geoiprest.adapters.db

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers

class ParserTest extends AsyncFlatSpec with AsyncIOSpec with Matchers {

  // behavior of "parseLine"

  it should "parse a ling of CSV into an IpRangeGeoInfo" in {
    val line =
      """31.31.91.0,31.31.91.255,ES,Valencia,,Port de Sagunt,,39.6621,-0.228449,"""
        .trim()

    val result = Parser.parseLine[IO](line)
    result.asserting { info =>
      info.ipRange.start.toString() shouldEqual "31.31.91.0"
      info.ipRange.end.toString() shouldEqual "31.31.91.255"
      info.countryCode shouldEqual "ES"
      info.stateProv shouldEqual "Valencia"
      info.city shouldEqual "Port de Sagunt"
      info.latitude shouldEqual 39.6621
      info.longitude shouldEqual -0.228449
    }
  }
}
