package dev.albertinho.geoiprest.infra

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.Ignore
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers

@Ignore
class FabioLabella extends AsyncFlatSpec with AsyncIOSpec with Matchers {

  it should "foo" in {
    FabioLabella.process.compile.toList.asserting { l =>
      l should contain theSameElementsAs List("chzeck", "kafka")
    }
  }

}

object FabioLabella {

  def process = all.take(30).flatMap { s => fs2.Stream("unified" + s) }

  def all: fs2.Stream[IO, String] = fs2
    .Stream(
      healthCheck,
      kafkaMessages,
      celsiusConverter.drain
    )
    .parJoinUnbounded

  def healthCheck: fs2.Stream[IO, String] =
    fs2.Stream("check").repeat.covary[IO]

  def kafkaMessages: fs2.Stream[IO, String] =
    fs2.Stream("kafka").repeat.covary[IO]

  def celsiusConverter: fs2.Stream[IO, Unit] = fs2.Stream.eval(IO.unit)
}
