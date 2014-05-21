import sbt._
import Keys._

import sbtassembly.Plugin.assemblySettings
import spray.revolver.RevolverPlugin.Revolver

object ReleaseTrainBuild extends Build {
  val sprayV = "1.3.1"
  val akkaV = "2.3.2"

  lazy val root =
    Project("root", file("."))
      .aggregate(model, cachedRepository, ivyRepository, analysis, main)

  lazy val commonSettings: Def.SettingsDefinition = Seq(
    scalaVersion := "2.10.4",
    scalacOptions ++= Seq("-deprecation", "-unchecked", "-language:_", "-Xfatal-warnings", "-Ywarn-nullary-unit", "-Ywarn-dead-code"),
    libraryDependencies += "org.specs2" %% "specs2" % "2.2.2" % "test",
    name ~= (oldName => s"release-train-$oldName")
  ) ++ ScalariformSupport.formatSettings

  lazy val model =
    Project("model", file("model"))
      .settings(commonSettings: _*)
      .settings(
        libraryDependencies ++= Seq(
          "io.spray" % "spray-http" % sprayV
        )
      )

  lazy val cachedRepository =
    Project("cached", file("cached"))
      .settings(commonSettings: _*)
      .settings(
        libraryDependencies += "io.spray" %% "spray-json" % "1.2.6"
      )
      .dependsOn(model)

  lazy val ivyRepository =
    Project("ivy", file("ivy"))
      .settings(commonSettings: _*)
      .settings(
        libraryDependencies += "org.scala-sbt" % "ivy" % "0.13.1"
      )
      .dependsOn(model)

  lazy val analysis =
    Project("analysis", file("analysis"))
      .settings(commonSettings: _*)
      .dependsOn(model)

  lazy val main =
    Project("main", file("main"))
      .settings(commonSettings: _*)
      .settings(Revolver.settings: _*)
      .settings(assemblySettings: _*)
      .settings(
        libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.0.13" % "runtime"
      )
      .dependsOn(model, analysis, ivyRepository, cachedRepository)
}
