package net.virtualvoid.release
package ivy

import net.virtualvoid.release.model.{ Revision, ModuleID, ScalaVersion, Library }
import sbt._
import java.io.File
import sbt.MavenRepository

trait IvyInterface {
  def findVersion(library: Library, scalaVersion: ScalaVersion): Seq[ModuleID]
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
  val config = new InlineIvyConfiguration(
    new IvyPaths(new File("."), None),
    Seq(DefaultMavenRepository, sprayResolver, typesafeResolver), Nil, Nil, false, None, Nil, None, logger)
  val ivy = new IvySbt(config)

  def findVersion(library: Library, scalaVersion: ScalaVersion): Seq[ModuleID] =
    ivy.withIvy(logger) { ivy ⇒
      val moduleName = crossVersionModule(library.module, scalaVersion.version)
      val revisions =
        ivy.getSearchEngine
          .listRevisions(library.organization, moduleName).toSeq

      revisions.map(rev ⇒ ModuleID(library.organization, moduleName, Revision(rev)))
    }

  def crossVersionModule(module: String, version: String): String = s"${module}_$version"
}

object IvyTest extends App {
  val targetVersion = ScalaVersion.`2.10`
  //val targetVersion = ScalaVersion.`2.11.0-RC3`
  def report(lib: Library): Unit = {
    import ExtraMethods._
    val versions = IvyImplementation.findVersion(lib, targetVersion)
    import lib._
    println(f"$name%-30s ${versions.size}%3d versions${versions.map(_.revision).latest.fold("")(v ⇒ s", latest: $v")}")
  }

  Libraries.all.foreach(report)
}