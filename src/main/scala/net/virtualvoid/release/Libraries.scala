package net.virtualvoid.release

import net.virtualvoid.release.model.{ ModuleDef, Library }

object Libraries {
  // TYPESAFE
  val akkaActor = lib("akka-actor", "com.typesafe.akka", "akka-actor")
  val slick = lib("slick", "com.typesafe", "slick")

  val scalaXml = lib("scala-xml", "org.scala-lang.modules", "scala-xml")
  val scalaParserCombinators = lib("scala-parser-combinators", "org.scala-lang.modules", "scala-parser-combinators")
  val scalaSwing = lib("scala-swing", "org.scala-lang.modules", "scala-swing")
  val async = lib("async", "org.scala-lang.modules", "scala-async")

  // TESTING
  val scalaTest = lib("scalatest", "org.scalatest", "scalatest")
  val specs2 = lib("specs2", "org.specs2", "specs2-core")
  val scalacheck = lib("scalacheck", "org.scalacheck", "scalacheck")

  // DB
  val squeryl = lib("squeryl", "org.squeryl", "squeryl")

  // JSON
  val sprayJson = lib("spray-json", "io.spray", "spray-json")
  val json4s = lib("json4s-native", "org.json4s", "json4s-native")
  val liftJson = lib("lift-json", "net.liftweb", "lift-json")
  val playJson = lib("play-json", "com.typesafe.play", "play-json")

  // INFRASTRUCTURE + OTHERS
  val spire = lib("spire", "org.spire-math", "spire")
  val scalazCore = lib("scalaz", "org.scalaz", "scalaz-core")
  val shapeless = lib("shapeless", "com.chuusai", "shapeless")
  val parboiled = lib("parboiled", "org.parboiled", "parboiled-scala")
  val scopt = lib("scopt", "com.github.scopt", "scopt")
  val scalaArm = lib("scala-arm", "com.jsuereth", "scala-arm")

  val kiama = lib("kiama", "com.googlecode.kiama", "kiama")

  val finagle = lib("finagle", "com.twitter", "finagle")
  val ostrich = lib("ostrich", "com.twitter", "ostrich")
  val algebird = lib("algebird", "com.twitter", "algebird")
  val scalding = lib("scalding", "com.twitter", "scalding")

  val all: Seq[Library] = Seq(
    akkaActor,
    slick,
    scalaXml,
    scalaParserCombinators,
    scalaSwing,
    async,

    scalaTest,
    specs2,
    scalacheck,

    squeryl,

    sprayJson,
    json4s,
    liftJson,
    playJson,

    spire,
    scalazCore,
    shapeless,
    parboiled,
    scopt,
    scalaArm,

    kiama,

    finagle,
    ostrich,
    algebird,
    scalding)

  def lib(name: String, organization: String, module: String, twitterHandle: Option[String] = None) =
    Library(name, ModuleDef(organization, module), twitterHandle)
}