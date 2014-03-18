libraryDependencies ++= {
  val sprayV = "1.3.1"
  val akkaV = "2.3.0"
  Seq(
    "io.spray" % "spray-routing" % sprayV,
    "io.spray" % "spray-can" % sprayV,
    "com.typesafe.akka" %% "akka-actor" % akkaV,
    "org.scala-sbt" % "ivy" % "0.13.1",
    "ch.qos.logback" % "logback-classic" % "1.0.13" % "runtime",
    "org.specs2" %% "specs2" % "2.2.2" % "test"
  )
}

scalaVersion := "2.10.3"

ScalariformSupport.formatSettings

Revolver.settings
