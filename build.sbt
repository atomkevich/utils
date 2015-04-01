name := "globus"

version := "1.0"

resolvers += "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/"

scalaVersion := "2.11.5"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= {
  val akkaV = "2.3.6"
  val sprayV = "1.3.2"
  Seq(
    "com.typesafe.play" %% "play-json" % "2.3.4",
    "com.typesafe.akka" %% "akka-slf4j" % akkaV,
    "org.mongodb" %% "casbah" % "2.8.0",
    "org.scalaj" %% "scalaj-http" % "1.1.3",
    "net.liftweb" %% "lift-json" % "2.6+",
    "org.json4s" %% "json4s-jackson" % "3.2.11",
    "org.json4s" %% "json4s-native" % "3.2.11",
    "io.spray"            %%  "spray-can"     % "1.3.2",
    "io.spray"            %%  "spray-routing" % sprayV,
    "io.spray"            %%  "spray-json"    % "1.3.1",
    "io.spray"            %%  "spray-testkit" % sprayV  % "test",
    "com.typesafe.akka"   %%  "akka-actor"    % akkaV,
    "com.typesafe.akka"   %%  "akka-testkit"  % akkaV   % "test",
    "org.specs2"          %%  "specs2-core"   % "2.3.11" % "test",
    "org.scalaz"          %%  "scalaz-core"   % "7.1.0",
    "org.mindrot" % "jbcrypt" % "0.3m",
    "org.scalatest" % "scalatest_2.11" % "2.2.1" % "test",
    "org.mockito" % "mockito-all" % "1.8.4",
    // -- Logging --
    "ch.qos.logback" % "logback-classic" % "1.1.2"
  )
}
