package net.virtualvoid.release
package model

import spray.http.DateTime

case class ScalaVersion(version: String) extends AnyVal
object ScalaVersion {
  val `2.10` = ScalaVersion("2.10")
  val `2.11.0-RC1` = ScalaVersion("2.11.0-RC1")
  val `2.11.0-RC3` = ScalaVersion("2.11.0-RC3")
  val `2.11` = ScalaVersion("2.11")
}

case class ModuleDef(organization: String, module: String)

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
  implicit def revisionOrdering: Ordering[Revision] = Ordering.by[Revision, String](_.revision)
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
