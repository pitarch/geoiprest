package dev.albertinho.geoiprest

import cats.effect.IO
import cats.effect.std.Queue
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.implicits._
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{Ignore, Inspectors}

@Ignore
class DistributionTest
    extends AsyncFlatSpec
    with AsyncIOSpec
    with Matchers
    with Inspectors {

  val SIZE = 256

  it should "distribute numbers to queues" in {
    val stream: fs2.Stream[IO, Int] =
      fs2.Stream.iterate(1)(_ + 1).take(20).covary[IO]

    val ioQueue: IO[Queue[IO, Option[Int]]] = Queue.bounded[IO, Option[Int]](1)

    val result = ioQueue.flatMap { queue =>
      val producer =
        stream.evalMap(i => queue.offer(Some(i))) ++ fs2.Stream.eval(
          queue.offer(None)
        )
      val consumer = fs2.Stream
        .fromQueueNoneTerminated(queue)
        .evalTap(IO.println)
        .onFinalize(IO.println("done"))
      val flow = consumer.concurrently(producer)
      flow.compile.toList
    }

    result.asserting(_ shouldBe (1 to 20).toList)
  }

  it should "distribute numbers into multiple queues" in {
    val STREAM_SIZE = 3075320L
    val stream: fs2.Stream[IO, Int] =
      fs2.Stream.iterate(1)(_ + 1).take(STREAM_SIZE).covary[IO]

    val ioQueues = Queue.bounded[IO, Option[Int]](1000).replicateA(SIZE)

    val result = ioQueues.flatMap { queues =>
      val queueVector = queues.toVector
      val producerFinalizer = fs2.Stream
        .iterate(0)(_ + 1)
        .take(SIZE.toLong)
        .evalMap(queueIndex => queueVector(queueIndex).offer(None))

      val producer: fs2.Stream[IO, Unit] = stream
        // .evalTap(value => IO.println(s"producing $value from ${Thread.currentThread().getName}"))
        .evalMap(i => queues(i % SIZE).offer(Some(i))) ++ producerFinalizer

      def consumer(
          queue: Queue[IO, Option[Int]],
          index: Int
      ): IO[(Int, List[Int])] = fs2.Stream
        .fromQueueNoneTerminated(queue)
        // .evalTap(value => IO.println(s"consumer $index: $value (${Thread.currentThread().getName})"))
        .onFinalize(IO.println(s"consumer $index finished"))
        .compile
        .toList
        .map((index, _))

      val consumers: List[IO[(Int, List[Int])]] = queues.zipWithIndex.map {
        case (queue, index) => consumer(queue, index)
      }
      // launch all consumers concurrently and flatten the result
      val consumerStreams = consumers.map { c => fs2.Stream.eval(c).covary[IO] }
      fs2
        .Stream(consumerStreams :+ producer.drain: _*)
        .parJoinUnbounded
        .compile
        .toList
        .map(_.toMap)
    }

    result.asserting { hashMap =>
      hashMap.size shouldBe SIZE
      hashMap.keys.toSet shouldBe (0 until SIZE).toSet
      forAll(hashMap.keys) { key =>
        forAll(hashMap(key)) { value =>
          value % SIZE shouldBe key
        }
      }
    }
  }
}
