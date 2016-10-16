package net.virtualvoid.release

import net.virtualvoid.release.model.{ ModuleDef, Library }

object Libraries {
  // TYPESAFE
  val akkaActor = lib("akka-actor", "com.typesafe.akka", "akka-actor")
  val akkaStream = lib("akka-stream", "com.typesafe.akka", "akka-stream")
  val akkaHttp = lib("akka-http", "com.typesafe.akka", "akka-http-experimental")
  val akkaOsgi = lib("akka-osgi", "com.typesafe.akka", "akka-osgi")
  val akkaSlf4j = lib("akka-slf4j", "com.typesafe.akka", "akka-slf4j")
  val akkaTestkit = lib("akka-testkit", "com.typesafe.akka", "akka-testkit")
  val slick = lib("slick", "com.typesafe.slick", "slick")

  val scalaXml = lib("scala-xml", "org.scala-lang.modules", "scala-xml")
  val scalaParserCombinators = lib("scala-parser-combinators", "org.scala-lang.modules", "scala-parser-combinators")
  val scalaSwing = lib("scala-swing", "org.scala-lang.modules", "scala-swing")
  val async = lib("async", "org.scala-lang.modules", "scala-async")
  val scalaLoggingSlf4f = lib("scala-logging-slf4j", "com.typesafe.scala-logging", "scala-logging-slf4j")
  val macroParadise = lib("macro-paradise", "org.scalamacros", "paradise")

  // TESTING
  val scalaTest = lib("scalatest", "org.scalatest", "scalatest")
  val specs2 = lib("specs2", "org.specs2", "specs2-core")
  val scalacheck = lib("scalacheck", "org.scalacheck", "scalacheck")

  // DB
  val squeryl = lib("squeryl", "org.squeryl", "squeryl")
  val scalikejdbInterpolation = lib("scalikejdbc-interpolation", "org.scalikejdbc", "scalikejdbc-interpolation")

  // JSON
  val sprayJson = lib("spray-json", "io.spray", "spray-json")
  val json4s = lib("json4s-native", "org.json4s", "json4s-native")
  val liftJson = lib("lift-json", "net.liftweb", "lift-json")
  val playJson = lib("play-json", "com.typesafe.play", "play-json")
  val twitterJson = lib("twitter-json", "com.twitter", "scala-json")
  val argonaut = lib("argonaut", "io.argonaut", "argonaut")
  val raptureIO = lib("raptureIO", "com.propensive", "rapture-io")

  val jsonLenses = lib("json-lenses", "net.virtual-void", "json-lenses")

  // INFRASTRUCTURE + OTHERS
  val spire = lib("spire", "org.spire-math", "spire")
  val algebra = lib("algebra", "org.spire-math", "algebra")
  val debox = lib("debox", "org.spire-math", "debox")
  val kindProjector = lib("kind-projector", "org.spire-math", "kind-projector")
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
  val scalateCore = lib("scalate-core", "org.scalatra.scalate", "scalate-core")
  val ghscalaCore = lib("ghscala", "com.github.xuwei-k", "ghscala-core")
  val platformExecuting = lib("platform-executing", "com.nocandysw", "platform-executing")
  val nscalaTime = lib("nscala-time", "com.github.nscala-time", "nscala-time")
  val nalloc = lib("nalloc", "org.nalloc", "optional")
  val f0 = lib("f0", "com.clarifi", "f0")
  val scrimage = lib("scrimage", "com.sksamuel.scrimage", "scrimage")
  val monifu = lib("monifu", "org.monifu", "monifu")
  val macwire = lib("MacWire", "com.softwaremill.macwire", "macros")
  val scalamacrodebug = lib("scalamacrodebug", "com.softwaremill.scalamacrodebug", "macros")

  val kiama = lib("kiama", "com.googlecode.kiama", "kiama")

  val scalactic = lib("scalactic", "org.scalactic", "scalactic")

  val finagleCore = lib("finagle-core", "com.twitter", "finagle-core")
  val ostrich = lib("ostrich", "com.twitter", "ostrich")
  val algebirdCore = lib("algebird-core", "com.twitter", "algebird-core")
  val scaldingCore = lib("scalding-core", "com.twitter", "scalding-core")

  val dispatchCore = lib("dispatch-core", "net.databinder", "dispatch-core")
  val unfiltered = lib("unfiltered", "net.databinder", "unfiltered")

  val sprayRouting = lib("spray-routing", "io.spray", "spray-routing")
  val sprayCan = lib("spray-can", "io.spray", "spray-can")

  val twirlApi = lib("twirl-api", "io.spray", "twirl-api")

  val fastParse = lib("fastparse", "com.lihaoyi", "fastparse")

  val jawnParser = lib("jawn-parser", "org.spire-math", "jawn-parser")
  val jawnAst = lib("jawn-ast", "org.spire-math", "jawn-ast")
  val jawnArgonaut = lib("jawn-argonaut", "org.spire-math", "jawn-argonaut")
  val jawnJson4s = lib("jawn-json4s", "org.spire-math", "jawn-json4s")
  val jawnPlay = lib("jawn-play", "org.spire-math", "jawn-play")
  val jawnRojoma = lib("jawn-rojoma", "org.spire-math", "jawn-rojoma")
  val jawnSpray = lib("jawn-spray", "org.spire-math", "jawn-spray")

  val monocleCore = lib("monocle-core", "com.github.julien-truffaut", "monocle-core")
  val monocleMacro = lib("monocle-macro", "com.github.julien-truffaut", "monocle-macro")

  val fs2 = lib("fs2", "co.fs2", "fs2-core")

  val alleycats = lib("org.typelevel", "alleycats")
  val catalysts = lib("org.typelevel", "catalysts")
  val cats = lib("org.typelevel", "cats")
  val discipline = lib("org.typelevel", "discipline")
  val dogs = lib("org.typelevel", "dogs")
  val machinist = lib("org.typelevel", "machinist")

  val scodecCore = lib("org.typelevel", "scodec-core")
  val scodecBits = lib("org.typelevel", "scodec-bits")
  val scodecScalaz = lib("org.typelevel", "scodec-scalaz")
  val scodecStream = lib("org.typelevel", "scodec-stream")
  val scodecProtocols = lib("org.typelevel", "scodec-protocols")
  val scodecSpire = lib("org.typelevel", "scodec-spire")

  val scodecCoreOld = lib("org.scodec", "scodec-core")
  val scodecBitsOld = lib("org.scodec", "scodec-bits")

  val simulacrum = lib("com.github.mpilquist", "simulacrum")
  val enumeratum = lib("com.beachape", "enumeratum")

  val all: Seq[Library] = Seq(
    akkaActor,
    akkaStream,
    akkaHttp,
    akkaOsgi,
    akkaSlf4j,
    akkaTestkit,
    slick,
    scalaXml,
    scalaParserCombinators,
    scalaSwing,
    async,
    scalaLoggingSlf4f,
    //macroParadise, // cross-published with full version

    scalaTest,
    specs2,
    scalacheck,

    squeryl,
    scalikejdbInterpolation,

    sprayJson,
    json4s,
    liftJson,
    playJson,
    twitterJson,
    argonaut,
    raptureIO,

    jsonLenses,

    spire,
    algebra,
    debox,
    kindProjector,
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
    // ghscalaCore, still no 2.11 version as of 2015-06-12
    platformExecuting,
    nscalaTime,
    nalloc,
    //f0, still no 2.11 version as of 2015-06-12
    scrimage,
    monifu,
    macwire,
    scalamacrodebug,

    kiama,

    scalactic,

    finagleCore,
    ostrich,
    algebirdCore,
    scaldingCore,

    dispatchCore,
    unfiltered,

    sprayRouting,
    sprayCan,

    twirlApi,

    fastParse,

    jawnParser,
    jawnAst,
    jawnArgonaut,
    jawnJson4s,
    jawnPlay,
    jawnRojoma,
    jawnSpray,

    monocleCore,
    monocleMacro,

    fs2,

    // typelevel projects
    alleycats,
    catalysts,
    cats,
    discipline,
    dogs,
    machinist,

    scodecBits,
    scodecCore,
    scodecProtocols,
    scodecScalaz,
    scodecSpire,
    scodecStream,

    scodecCoreOld,
    scodecBitsOld,

    simulacrum,
    enumeratum)

  def lib(name: String, organization: String, module: String, twitterHandle: Option[String] = None): Library =
    Library(name, ModuleDef(organization, module), twitterHandle)

  def lib(organization: String, module: String): Library =
    lib(module, organization, module)

  require(all.map(_.name).distinct.size == all.size, "Duplicate name detected")
}