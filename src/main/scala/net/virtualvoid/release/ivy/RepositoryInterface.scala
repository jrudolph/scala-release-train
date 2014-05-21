package net.virtualvoid.release
package ivy

import model.{ Revision, ModuleID, ScalaVersion, Library, ModuleDef }
import sbt._
import java.io.File
import sbt.MavenRepository
import org.apache.ivy.core.resolve.ResolveOptions
import org.apache.ivy.util.filter.Filter
import org.apache.ivy.core.module.id.ModuleRevisionId
import spray.http.DateTime
import net.virtualvoid.release.model
import scala.annotation.tailrec
import spray.json.RootJsonFormat

trait RepositoryInterface {
  def findVersion(library: Library, scalaVersion: ScalaVersion): Seq[ModuleID] =
    findVersion(library.organization, library.module, scalaVersion)
  def findVersion(module: ModuleDef, scalaVersion: ScalaVersion): Seq[ModuleID] =
    findVersion(module.organization, module.module, scalaVersion)
  def findVersion(organization: String, module: String, scalaVersion: ScalaVersion): Seq[ModuleID] =
    findJavaVersion(ModuleDef(organization, crossVersionModule(organization, module, scalaVersion.version)))

  def crossVersionModule(organization: String, module: String, version: String): String =
    if (module.startsWith("spray") && !module.endsWith("json") && version == "2.10") module
    //else if (organization.startsWith("org.scala-lang") && version == "2.10") module
    else s"${module}_$version"

  def findJavaVersion(module: ModuleDef): Seq[ModuleID]
  def resolve(module: ModuleID): model.ModuleInfo
}

object ExtraMethods {
  implicit class EnrichRevisionSeq(val revs: Seq[Revision]) extends AnyVal {
    def latest: Option[Revision] =
      if (revs.isEmpty) None
      else Some(revs.max)
  }
}

