name := "WeatherAnalysis"

version := "0.1"

scalaVersion := "2.12.11"

val akkHttpVersion = "10.1.10"

val akkaVersion = "2.5.23"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-persistence" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
  "org.fusesource.leveldbjni" % "leveldbjni-all" % "1.8",
  "org.scalatest" %% "scalatest" % "3.1.2" % Test,
  "com.github.dnvriend" %% "akka-persistence-inmemory" % "2.5.15.2",
  "com.typesafe.akka" %% "akka-http" % akkHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkHttpVersion,
  "com.typesafe.akka" %% "akka-http-testkit" % akkHttpVersion
)