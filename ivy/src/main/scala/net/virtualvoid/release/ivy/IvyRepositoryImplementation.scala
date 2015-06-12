package net.virtualvoid.release.ivy

import java.io.File

import sbt._

import net.virtualvoid.release.model.{ Revision, ModuleID, ModuleDef }
import org.apache.ivy.core.module.id.ModuleRevisionId
import org.apache.ivy.core.resolve.ResolveOptions
import spray.http.DateTime
import org.apache.ivy.util.filter.Filter
import net.virtualvoid.release.{ model, RepositoryInterface }

class IvyRepositoryImplementation(logger: sbt.Logger) extends RepositoryInterface {
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

object IvyRepositoryImplementation {
  def apply(quiet: Boolean): RepositoryInterface = {
    val logger = if (quiet) NoLogger else sbt.ConsoleLogger(Console.err)
    new IvyRepositoryImplementation(logger)
  }
}