package dev.albertinho.geoiprest.infra.db

import cats.effect.IO
import cats.effect.kernel.Resource
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.Inspectors
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers

class LoaderTest
    extends AsyncFlatSpec
    with AsyncIOSpec
    with Matchers
    with Inspectors {

  private val content = """
    |1.0.0.0,1.0.0.255,AU,Queensland,,South Brisbane,,-27.4767,153.017,
    |1.0.1.0,1.0.3.255,CN,Fujian,,Gaosha,,26.4837,117.925,
    """.stripMargin

  behavior of "string content"

  it should "be read and parsed when empty" in {
    val content = ""
    val loader = Loader.fromString[IO](content)
    loader.stream.compile.toList.asserting { result =>
      result shouldBe empty
    }
  }

  it should "be read and parsed when non-empty" in {

    val loader = Loader.fromString[IO](content)
    val resultIO = loader.stream.compile.toList
    resultIO.asserting { result =>
      result should have length 2
      val linesNumbers = result.map(_._1)
      val infos = result.map(_._2)
      linesNumbers shouldBe List(1, 2)
      forAll(infos) { info =>
        info.isSuccess shouldBe true
      }
    }
  }

  behavior of "fromFile"

  it should "read and parse" in {
    createFileResourceWithContent(content).use { path =>
      val loader = Loader.fromFile[IO](path.toString)
      val resultIO = loader.stream.compile.toList
      resultIO.asserting { result =>
        result should have length 2
        val linesNumbers = result.map(_._1)
        val infos = result.map(_._2)
        linesNumbers shouldBe List(1, 2)
        forAll(infos) { info =>
          info.isSuccess shouldBe true
        }
      }
    }
  }

  private def createFileResourceWithContent(content: String) = {
    val acquire = IO {
      val path = java.nio.file.Files.createTempFile("test", "csv")
      java.nio.file.Files.write(path, content.getBytes)
      path
    }
      .flatTap { path => IO(println(s"Created file: $path")) }

    val release = (path: java.nio.file.Path) =>
      IO(java.nio.file.Files.deleteIfExists(path)).void
        .flatTap { _ => IO(println(s"Deleted file: $path")) }

    Resource.make(acquire)(release)
  }
}
