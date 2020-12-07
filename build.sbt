
organization := "com.quasigroup.inc"

name := "zio-akka"

version := "0.1"

scalaVersion := "2.13.4"

libraryDependencies ++= akka ++ http ++ zio

lazy val zio = {
  val version = "1.0.3"
  Seq(
    "dev.zio" %% "zio" % version,
    "dev.zio" %% "zio-interop-cats" % "2.2.0.1",
    "dev.zio" %% "zio-logging" % "0.4.0",
    "dev.zio" %% "zio-logging-slf4j" % "0.4.0",
  )
}

lazy val akka = {
  val akkaVersion = "2.6.10"
  Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
    "com.typesafe.akka" %% "akka-stream" % akkaVersion,
    "com.typesafe.akka" %% "akka-stream-typed"  % akkaVersion,
    "com.typesafe.akka" %% "akka-cluster"  % akkaVersion,
    "com.typesafe.akka" %% "akka-cluster-typed"  % akkaVersion,
    "com.typesafe.akka" %% "akka-cluster-sharding" % akkaVersion,
    "com.typesafe.akka" %% "akka-cluster-sharding-typed" % akkaVersion,
    "com.typesafe.akka" %% "akka-cluster-metrics" % akkaVersion,
    "com.typesafe.akka" %% "akka-cluster-tools" % akkaVersion,
    "com.typesafe.akka" %% "akka-persistence" % akkaVersion excludeAll (ExclusionRule(
      "io.netty"
    )),
    "com.typesafe.akka" %% "akka-persistence-typed" % akkaVersion excludeAll (ExclusionRule(
      "io.netty"
    )),
    "com.typesafe.akka" %% "akka-persistence-query" % akkaVersion,
    "com.typesafe.akka" %% "akka-distributed-data" % akkaVersion,
    "com.typesafe.akka" %% "akka-multi-node-testkit" % akkaVersion,
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
    "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test
  )
}

lazy val http = {
  val akkaHttpVersion = "10.2.0"
  Seq(
    "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion,
    "de.heikoseeberger" %% "akka-http-circe" % "1.28.0",
    "ch.megard" %% "akka-http-cors" % "0.4.3",
  )
}
