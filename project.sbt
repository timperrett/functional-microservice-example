
organization in Global  := "com.timperrett.example"

scalaVersion in Global  := "2.11.6"

resolvers in Global ++= Seq(
  Resolver.bintrayRepo("oncue", "releases"),
  Resolver.bintrayRepo("scalaz", "releases"),
  "sonatype.oss" at "https://oss.sonatype.org/content/repositories/releases",
  "tpolecat" at "http://dl.bintray.com/tpolecat/maven"
)

lazy val example = project.in(file(".")).aggregate(
  core,
  http
)

lazy val core = project

lazy val http = project.dependsOn(core)
