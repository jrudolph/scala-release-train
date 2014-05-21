package net.virtualvoid.release
package model

import spray.http.DateTime

case class ScalaVersion(version: String) extends AnyVal {
  override def toString: String = s"Scala $version"
}
object ScalaVersion {
  val `2.10` = ScalaVersion("2.10")
  val `2.11.0-RC1` = ScalaVersion("2.11.0-RC1")
  val `2.11.0-RC3` = ScalaVersion("2.11.0-RC3")
  val `2.11.0-RC4` = ScalaVersion("2.11.0-RC4")
  val `2.11` = ScalaVersion("2.11")
}

case class ModuleDef(organization: String, module: String) {
  override def toString: String = s"$organization % $module"
}

case class Library(
    name: String,
    moduleDef: ModuleDef,
    developerTwitterHandle: Option[String]) {
  def organization: String = moduleDef.organization
  def module: String = moduleDef.module
}

// wrapper class to simplify adding orderings and stuff
case class Revision(revision: String) /*extends AnyVal*/ {
  override def toString: String = revision
}
object Revision {
  val Version = """(\d+(?:\.\d+)*)(?:-(.*))?""".r
  trait Suffix {
    def unapply(str: String): Option[String]
  }
  def Suffix(prefix: String): Suffix =
    new Suffix {
      val p = prefix.toLowerCase

      def unapply(str: String): Option[String] =
        if (str.toLowerCase.startsWith(p)) Some(str) else None
    }

  val Milestone = Suffix("m")
  val Alpha = Suffix("alpha")
  val Beta = Suffix("beta")
  val RC = Suffix("rc")

  implicit def revisionOrdering: Ordering[Revision] = Ordering.by[Revision, (String, Int, String)] {
    _.revision match {
      case Version(v, null)              ⇒ (v, 0, "") // full version
      case Version(v, RC(suffix))        ⇒ (v, -1, suffix)
      case Version(v, Beta(suffix))      ⇒ (v, -2, suffix)
      case Version(v, Alpha(suffix))     ⇒ (v, -3, suffix)
      case Version(v, Milestone(suffix)) ⇒ (v, -4, suffix)
      case v                             ⇒ (v, -100, "")
    }
  }
}

case class ModuleID(organization: String, module: String, revision: Revision) {
  /** Strip off cross-version suffix */
  def rawModule: String = {
    val idx = module.lastIndexOf('_')
    if (idx >= 0) module.substring(0, idx)
    else module
  }
  def asDef: ModuleDef = ModuleDef(organization, rawModule)
}
case class ModuleInfo(
    module: ModuleID,
    releaseTimestamp: DateTime,
    dependencies: Seq[ModuleID]) {
  def compatibleWith(version: ScalaVersion): Boolean =
    scalaVersion.forall(_ == version)
  def scalaVersion: Option[ScalaVersion] =
    dependencies.find(_.module == "scala-library").map(v ⇒ ScalaVersion(v.revision.revision))
}
