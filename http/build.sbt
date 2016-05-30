val circeVersion = "0.4.0"

val http4sVersion = "0.13.2"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "io.circe" %% "circe-java8" % circeVersion,
  "oncue.knobs" %% "core" % "3.6.1",
  "org.http4s" %% "http4s-server" % http4sVersion,
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "org.http4s" %% "http4s-blaze-client" % http4sVersion,
  "org.http4s" %% "http4s-circe" % http4sVersion,
  "ch.qos.logback" % "logback-classic" % "1.1.3",
  "org.apache.commons" % "commons-lang3" % "3.0",
  "org.scalatest" %% "scalatest"   % "2.2.5" % "test"
)

//fork in run := true

//connectInput in run := true