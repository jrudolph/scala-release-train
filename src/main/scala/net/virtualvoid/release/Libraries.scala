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
  val twitterJson = lib("twitter-json", "com.twitter", "scala-json")
  val argonaut = lib("argonaut", "io.argonaut", "argonaut")

  val jsonLenses = lib("json-lenses", "net.virtual-void", "json-lenses")

  // INFRASTRUCTURE + OTHERS
  val spire = lib("spire", "org.spire-math", "spire")
  val scalazCore = lib("scalaz", "org.scalaz", "scalaz-core")
  val shapeless = lib("shapeless", "com.chuusai", "shapeless")
  val parboiled = lib("parboiled", "org.parboiled", "parboiled-scala")
  val parboiled2 = lib("parboiled2", "org.parboiled", "parboiled")
  val scopt = lib("scopt", "com.github.scopt", "scopt")
  val scalaArm = lib("scala-arm", "com.jsuereth", "scala-arm")
  val scalaStm = lib("scala-stm", "org.scala-stm", "scala-stm")
  val scallop = lib("scallop", "org.rogach", "scallop")
  val scalafx = lib("scalafx", "org.scalafx", "scalafx")
  val scalatra = lib("scalatra", "org.scalatra", "scalatra")
  val scalateCore = lib("scalate-core", "org.fusesource.scalate", "scalate-core")
  val ghscalaCore = lib("ghscala", "com.github.xuwei-k", "ghscala-core")
  val platformExecuting = lib("platform-executing", "com.nocandysw", "platform-executing")

  val kiama = lib("kiama", "com.googlecode.kiama", "kiama")

  val finagleCore = lib("finagle-core", "com.twitter", "finagle-core")
  val ostrich = lib("ostrich", "com.twitter", "ostrich")
  val algebirdCore = lib("algebird-core", "com.twitter", "algebird-core")
  val scaldingCore = lib("scalding-core", "com.twitter", "scalding-core")

  val dispatchCore = lib("dispatch-core", "net.databinder", "dispatch-core")
  val unfiltered = lib("unfiltered", "net.databinder", "unfiltered")

  val sprayRouting = lib("spray-routing", "io.spray", "spray-routing")
  val sprayCan = lib("spray-can", "io.spray", "spray-can")

  val twirlApi = lib("twirl-api", "io.spray", "twirl-api")

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
    twitterJson,
    argonaut,

    jsonLenses,

    spire,
    scalazCore,
    shapeless,
    parboiled,
    parboiled2,
    scopt,
    scalaArm,
    scalaStm,
    scallop,
    scalafx,
    scalatra,
    scalateCore,
    ghscalaCore,
    platformExecuting,

    kiama,

    finagleCore,
    ostrich,
    algebirdCore,
    scaldingCore,

    dispatchCore,
    unfiltered,

    sprayRouting,
    sprayCan,

    twirlApi)

  def lib(name: String, organization: String, module: String, twitterHandle: Option[String] = None) =
    Library(name, ModuleDef(organization, module), twitterHandle)
}