package dev.albertinho.geoiprest

import org.scalatest.flatspec.AsyncFlatSpec
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.matchers.should.Matchers
import cats.effect.IO

class CatsEffectTestingScalaTestTest
    extends AsyncFlatSpec
    with AsyncIOSpec
    with Matchers {

  it should "test" in {
    IO.pure(1).map(_ + 1).asserting { _ shouldBe 2 }
  }
}
