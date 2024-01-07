package dev.albertinho.geoiprest.adapters.http

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.implicits.catsSyntaxOptionId
import dev.albertinho.geoiprest.domain.models.{IpRangeGeoInfo, Ipv4, Ipv4Range}
import dev.albertinho.geoiprest.ports.GeoipService
import io.circe.Json
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
      body <- response.as[Json]
    } yield (response.status, body)

    ioResult.asserting { case (status, body) =>
      status.code shouldBe 200
      body.hcursor
        .downField("countryCode")
        .as[String].toOption shouldBe expectedGeoipInfo.get.countryCode.some
      body.hcursor
        .downField("stateProv")
        .as[String].toOption shouldBe expectedGeoipInfo.get.stateProv.some
      body.hcursor
        .downField("city")
        .as[String].toOption shouldBe expectedGeoipInfo.get.city.some
      body.hcursor
        .downField("latitude")
        .as[Double].toOption shouldBe expectedGeoipInfo.get.latitude.some
      body.hcursor
        .downField("longitude")
        .as[Double].toOption shouldBe expectedGeoipInfo.get.longitude.some
      body.hcursor
        .downField("startAddress")
        .as[String].toOption shouldBe expectedGeoipInfo.get.ipRange.start.toString().some
      body.hcursor
        .downField("endAddress")
        .as[String].toOption shouldBe expectedGeoipInfo.get.ipRange.end.toString().some
    }
  }

  it should "return bad request error when malformed input ip" in {
    val process = createRequestProcessor(None)
    val request = Request[IO](uri = uri"/geoip/1.2.3")
    val ioResult = for {
      response <- process(request)
      body <- response.as[String]
    } yield (response.status, body)

    ioResult.asserting { case (status, body) =>
      status.code shouldBe 400
      body shouldBe "Malformed ip: 1.2.3"
    }
  }

  private val genGeoipInfo = for {
    start <- Gen.long
    end <- Gen.choose(start, Long.MaxValue)
    countryCode <- Gen.alphaNumStr.map(_.take(3))
    state <- Gen.alphaStr.map(_.take(5))
    city <- Gen.alphaStr.map(_.take(5))
    ipRange = Ipv4Range(Ipv4.fromLong(start), Ipv4.fromLong(end))
  } yield IpRangeGeoInfo(
    ipRange,
    s"countryCode$countryCode",
    s"state$state",
    s"city$city",
    0.0d,
    0.0d
  )

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
