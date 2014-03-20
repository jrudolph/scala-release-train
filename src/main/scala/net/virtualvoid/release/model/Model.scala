package net.virtualvoid.release
package model

import spray.http.DateTime

case class ScalaVersion(version: String)
object ScalaVersion {
  val `2.10` = ScalaVersion("2.10")
  val `2.11.0-RC1` = ScalaVersion("2.11.0-RC1")
  val `2.11.0-RC3` = ScalaVersion("2.11.0-RC3")
  val `2.11` = ScalaVersion("2.11")
}

case class LibraryRef(id: String)

case class Library(
    id: LibraryRef,
    organization: String,
    module: String,
    developerTwitterHandle: Option[String]) {
  def name: String = id.id
}

// wrapper class to simplify adding orderings and stuff
case class Revision(revision: String) extends AnyVal {
  override def toString: String = revision
}
object Revision {
  implicit def revisionOrdering: Ordering[Revision] = Ordering.by[Revision, String](_.revision)
}

case class ModuleID(organization: String, module: String, revision: Revision)
case class ModuleInfo(
    module: ModuleID,
    scalaVersion: Option[ScalaVersion],
    releaseTimestamp: DateTime,
    dependencies: Seq[ModuleID]) {
  def compatibleWith(version: ScalaVersion): Boolean =
    scalaVersion.forall(_ == version)
}

case class LibraryPublications(
    library: LibraryRef,
    timestamp: DateTime,
    module: Seq[ModuleID]) {
  def latest: ModuleID = ???
}