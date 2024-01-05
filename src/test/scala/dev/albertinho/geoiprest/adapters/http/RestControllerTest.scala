package dev.albertinho.geoiprest.adapters.http

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import dev.albertinho.geoiprest.domain.models.{IpRangeGeoInfo, Ipv4, Ipv4Range}
import dev.albertinho.geoiprest.ports.GeoipService
import io.circe.generic.auto._
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
import org.http4s.implicits.http4sLiteralsSyntax
import org.http4s.{Request, Response}
import org.scalacheck.Gen
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers

class RestControllerTest extends AsyncFlatSpec with Matchers with AsyncIOSpec {

  behavior of "RestController"

  it should "returns not-found when there is not matching geoip" in {
    val process = createRequestProcessor(None)
    val request = Request[IO](uri = uri"/geoip/1.2.3.4")
    process(request).asserting { response =>
      response.status.code shouldBe 404
    }
  }

  it should "return geoip when there is matching geoip" in {
    val expectedGeoipInfo = genGeoipInfo.sample
    val process = createRequestProcessor(expectedGeoipInfo)
    val request = Request[IO](uri = uri"/geoip/1.2.3.4")
    val ioResult = for {
      response <- process(request)
      body <- response.as[IpRangeGeoInfo]
    } yield (response.status, body)

    ioResult.asserting { case (status, body) =>
      status.code shouldBe 200
      body shouldBe expectedGeoipInfo.get
    }
  }

  it should "return bad request error when malformed input ip" in {
    val process = createRequestProcessor(None)
    val request = Request[IO](uri = uri"/geoip/1.2.3")
    process(request).asserting { response =>
      response.status shouldBe org.http4s.Status.BadRequest
    }
  }

  private val genGeoipInfo = for {
    start <- Gen.long
    end <- Gen.choose(start, Long.MaxValue)
    countryCode <- Gen.alphaNumStr.map(_.take(3))
    state <- Gen.alphaStr.map(_.take(5))
    city <- Gen.alphaStr.map(_.take(5))
    ipRange = Ipv4Range(Ipv4.fromLong(start), Ipv4.fromLong(end))
  } yield IpRangeGeoInfo(ipRange, s"countryCode$countryCode", s"state$state", s"city$city", 0.0d, 0.0d)

  private def createRequestProcessor(maybeGeoInfo: Option[IpRangeGeoInfo]) = {

    val service = new GeoipService[IO] {
      override def getInfo(ip: String): IO[Option[IpRangeGeoInfo]] =
        IO.pure(maybeGeoInfo)
    }
    val processRestRequest: Request[IO] => IO[Response[IO]] =
      new RestController[IO](service).routes.orNotFound.run

    processRestRequest
  }
}
