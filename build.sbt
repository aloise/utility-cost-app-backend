name := "UtilityCostAppBackend"

version := "1.0"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.8"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )

parallelExecution in Test := false

fork in Test := true

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"

libraryDependencies ++= Seq(
  cache,
  ws,
  evolutions,
  "com.typesafe.play" %% "play-slick" % "2.0.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "2.0.0",
  "com.typesafe.slick" %% "slick" % "3.1.1",
  "com.h2database" % "h2" % "1.4.191",
  "org.joda" % "joda-money" % "0.11",
  "org.julienrf" % "play-json-derived-codecs_2.11" % "3.3",
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.0" % Test
)