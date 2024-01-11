package dev.albertinho.geoiprest.adapters.http

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import com.comcast.ip4s.{Host, Port}
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
class RestServerConfigTest
    extends AsyncFlatSpec
    with Matchers
    with AsyncIOSpec {

  it should "load config from application.conf" in {
    val ioConfig = RestServerConfig.loadResource[IO]
    ioConfig.asserting { config =>
      config.host shouldBe a[Host]
      config.port shouldBe a[Port]
      // config.port should (be >= 0)
    }
  }
}
