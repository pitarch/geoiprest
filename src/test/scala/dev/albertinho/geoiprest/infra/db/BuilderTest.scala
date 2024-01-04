package dev.albertinho.geoiprest.infra.db

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import dev.albertinho.geoiprest.infra.db.BuilderTest.{createGeoInfo, createIpRangesWithSize}
import org.scalatest.OptionValues
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import cats.implicits._
import dev.albertinho.geoiprest.domain.models.{IpRangeGeoInfo, Ipv4, Ipv4Range}

class BuilderTest
    extends AsyncFlatSpec
    with Matchers
    with AsyncIOSpec
    with OptionValues {

  private lazy val builder = Builder.make[IO]


  it should "build an ordered sequence when stream is not empty" in {
    val geoInfoStream = createIpRangesWithSize().through(createGeoInfo).take(4)
    val ioDbFacade = builder.build(geoInfoStream)
    val ips = List("0.0.0.1", "0.0.0.255", "0.0.1.1").map(Ipv4.apply)
    val ioResults = for {
      dbFacade <- ioDbFacade
      maybeGeoInfos <- ips.map { ip => dbFacade.find(ip) }.sequence
    } yield maybeGeoInfos.toVector

    ioResults.asserting { results =>
      results should have length 3
      results.head.value.ipRange.start.toString shouldBe "0.0.0.0"
      results(1).value.ipRange.start.toString shouldBe "0.0.0.0"
      results(2).value.ipRange.start.toString shouldBe "0.0.1.0"
    }
  }
}

object BuilderTest {

  def createGeoInfo(
      stream: fs2.Stream[IO, Ipv4Range]
  ): fs2.Stream[IO, IpRangeGeoInfo] =
    stream.map { ipRange =>
      IpRangeGeoInfo(ipRange, s"country${ipRange.start}", "", "", 0.0d, 0.0d)
    }

  def createIpRangesWithSize(size: Int = 256): fs2.Stream[IO, Ipv4Range] =
    fs2.Stream
      .iterate[IO, Int](0)(_ + size)
      .map { start => Ipv4.fromLong(start.toLong) }
      .map { ipStart =>
        Ipv4Range(ipStart, Ipv4.fromLong(ipStart.toLong + size - 1))
      }
}
