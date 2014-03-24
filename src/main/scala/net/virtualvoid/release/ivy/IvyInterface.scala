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

trait IvyInterface {
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

object IvyImplementation extends IvyInterface {
  val logger = ConsoleLogger()
  val sprayResolver = MavenRepository("spray", "http://repo.spray.io/")
  val typesafeResolver = MavenRepository("Typesafe repository", "http://repo.typesafe.com/typesafe/releases/")
  val ossSonatype = MavenRepository("Sonatype releases", "http://oss.sonatype.org/content/repositories/releases/")
  val config = new InlineIvyConfiguration(
    new IvyPaths(new File("."), None),
    Seq(DefaultMavenRepository, sprayResolver, typesafeResolver, ossSonatype), Nil, Nil, false, None, Nil, None, logger)
  val ivy = new IvySbt(config)

  def findJavaVersion(module: ModuleDef): Seq[ModuleID] =
    ivy.withIvy(logger) { ivy ⇒
      val revisions =
        ivy.getSearchEngine
          .listRevisions(module.organization, module.module).toSeq

      revisions.map(rev ⇒ ModuleID(module.organization, module.module, Revision(rev)))
    }

  def resolve(module: ModuleID): model.ModuleInfo =
    ivy.withIvy(logger) { ivy ⇒
      val ivyModule: ModuleRevisionId = ModuleRevisionId.newInstance(module.organization, module.module, module.revision.revision)
      val options = new ResolveOptions
      options.setConfs(Array("compile", "provided", "test"))
      /*options.setArtifactFilter(filter {
        case a: org.apache.ivy.core.module.descriptor.Artifact ⇒
          println(a.getType)
          a.getType == "pom"
      })*/
      val res = ivy.getResolveEngine.resolve(ivyModule, options, false)
      val depModules = IvyRetrieve.reports(res).flatMap(c ⇒ IvyRetrieve.configurationReport(c).modules)
      val deps =
        depModules.map(m ⇒ ModuleID(m.module.organization, m.module.name, Revision(m.module.revision))).toSet - module
      model.ModuleInfo(module, DateTime.now, deps.toSeq)
    }

  def filter(f: PartialFunction[AnyRef, Boolean]): Filter =
    new Filter {
      def accept(o: AnyRef): Boolean = f.isDefinedAt(o) && f(o)
    }
}
