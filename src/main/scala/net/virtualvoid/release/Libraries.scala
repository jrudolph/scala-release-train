package net.virtualvoid.release

import model.{ LibraryRef, Library }

object Libraries {
  val all: Seq[Library] = Seq(
    lib("spray-json", "io.spray", "spray-json"),
    lib("parboiled", "org.parboiled", "parboiled-scala"),
    lib("akka-actor", "com.typesafe.akka", "akka-actor"),
    lib("shapeless", "com.chuusai", "shapeless"),
    lib("json4s-native", "org.json4s", "json4s-native"),
    lib("scalatest", "org.scalatest", "scalatest"),
    lib("specs2", "org.specs2", "specs2-core") //
    )

  def lib(name: String, organization: String, module: String, twitterHandle: Option[String] = None) =
    Library(LibraryRef(name), organization, module, twitterHandle)
}