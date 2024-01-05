val Http4sVersion = "0.23.24"
val CirceVersion = "0.14.6"
val LogbackVersion = "1.4.14"

lazy val root = (project in file("."))
  .settings(
    organization := "dev.albertinho",
    name := "geoip-rest",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.13.12",
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-ember-server" % Http4sVersion,
      "org.http4s" %% "http4s-ember-client" % Http4sVersion,
      "org.http4s" %% "http4s-circe" % Http4sVersion,
      "org.http4s" %% "http4s-dsl" % Http4sVersion,
      "io.circe" %% "circe-generic" % CirceVersion,
      "org.typelevel" %% "cats-effect-testing-scalatest" % "1.5.0" % Test,
      "org.scalacheck" %% "scalacheck" % "1.17.0" % Test,
      "ch.qos.logback" % "logback-classic" % LogbackVersion % Runtime
    ),
    testFrameworks += new TestFramework("org.scalatest.tools.Framework"),
    scalacOptions ~= { options: Seq[String] =>
      options.filterNot(
        Set(
          "-Wnonunit-statement"
        )
      )
    },
    addCompilerPlugin(
      "org.typelevel" %% "kind-projector" % "0.13.2" cross CrossVersion.full
    ),
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
    assembly / assemblyMergeStrategy := {
      case "module-info.class" => MergeStrategy.discard
      case x => (assembly / assemblyMergeStrategy).value.apply(x)
    }
  )
