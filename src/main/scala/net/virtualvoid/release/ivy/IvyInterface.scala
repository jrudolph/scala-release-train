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
    findJavaVersion(ModuleDef(organization, crossVersionModule(module, scalaVersion.version)))
  def crossVersionModule(module: String, version: String): String = s"${module}_$version"

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
  val config = new InlineIvyConfiguration(
    new IvyPaths(new File("."), None),
    Seq(DefaultMavenRepository, sprayResolver, typesafeResolver), Nil, Nil, false, None, Nil, None, logger)
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

sealed trait ModuleState {
  def isAvailable: Boolean
}
case class HasTargetVersion(versions: Seq[ModuleID]) extends ModuleState {
  def isAvailable = true
}
case class PreviousVersion(versions: Seq[ModuleID], latest: ModuleID, latestInfo: model.ModuleInfo) extends ModuleState {
  def isAvailable = false
}
case class JavaDependency(versions: Seq[ModuleID]) extends ModuleState {
  def isAvailable: Boolean = true // versions.nonEmpty ???
}
trait RepositoryInfo {
  def targetVersion: ScalaVersion
  def lastVersion: ScalaVersion
  def libraries: Seq[Library]

  def moduleStates: Map[ModuleDef, ModuleState]
  def moduleState(module: ModuleDef): ModuleState
  def moduleInfo(module: ModuleID): model.ModuleInfo
  def availableModules: Set[ModuleDef]
  def missingModules: Set[ModuleDef]
  def modulesForScalaVersion(module: ModuleDef, scalaVersion: ScalaVersion): Seq[ModuleID]

  def isAvailable(module: ModuleDef): Boolean = availableModules(module)
}
object RepositoryInfo {
  def gather(ivy: IvyInterface, _libraries: Seq[Library], _targetVersion: ScalaVersion, _lastVersion: ScalaVersion): RepositoryInfo = {
    val libraryModules: Seq[ModuleDef] = _libraries.map(_.moduleDef)

    def resolve(module: ModuleDef): ModuleState = {
      println(s"Resolving $module")
      val versions = ivy.findVersion(module, _targetVersion)
      if (versions.nonEmpty) HasTargetVersion(versions)
      else {
        val lastVersions = ivy.findVersion(module, _lastVersion)
        if (lastVersions.nonEmpty) {
          val latest = lastVersions.maxBy(_.revision)
          val info = ivy.resolve(latest)
          assert(info.scalaVersion.isDefined)
          PreviousVersion(lastVersions, latest, info)
        } else JavaDependency(ivy.findJavaVersion(module))
      }
    }

    @tailrec def resolveAll(toResolve: List[ModuleDef], states: Map[ModuleDef, ModuleState]): Map[ModuleDef, ModuleState] =
      toResolve match {
        case Nil                                     ⇒ states
        case first :: rest if states.contains(first) ⇒ resolveAll(rest, states)
        case first :: rest ⇒
          val state = resolve(first)
          val newStates = states.updated(first, state)
          state match {
            case p: PreviousVersion ⇒
              val deps = p.latestInfo.dependencies
              resolveAll(rest ++ deps.map(_.asDef), newStates)
            case _ ⇒ resolveAll(rest, newStates)
          }
      }

    val states = resolveAll(libraryModules.toList, Map.empty)

    new RepositoryInfo {
      def libraries: Seq[Library] = _libraries
      def targetVersion: ScalaVersion = _targetVersion
      def lastVersion: ScalaVersion = _lastVersion

      def moduleStates: Map[ModuleDef, ModuleState] = states
      def moduleState(module: ModuleDef): ModuleState = states(module)

      val availableModules: Set[ModuleDef] = states.filter(_._2.isAvailable).map(_._1).toSet
      val missingModules: Set[ModuleDef] = states.filterNot(_._2.isAvailable).map(_._1).toSet

      def moduleInfo(module: ModuleID): model.ModuleInfo = ???

      def modulesForScalaVersion(module: ModuleDef, scalaVersion: ScalaVersion): Seq[ModuleID] =
        states(module) match {
          case HasTargetVersion(versions) if scalaVersion == _targetVersion ⇒ versions
        }
    }
  }
}

object Analysis {
  def simpleMissingDependencyAnalysis(info: RepositoryInfo): Unit = {
    import ExtraMethods._

    val (available, missing) = info.libraries.partition(l ⇒ info.isAvailable(l.moduleDef))

    println(s"Libraries available for ${info.targetVersion}")
    println()
    available.foreach { lib ⇒
      import lib._
      val versions = info.modulesForScalaVersion(lib.moduleDef, info.targetVersion)
      println(f"$name%-30s ${versions.size}%3d versions${versions.map(_.revision).latest.fold("")(v ⇒ s", latest: $v")}")
    }
    println()

    println(s"Libraries missing")
    println()
    missing.foreach { lib ⇒

    }

    println()
    println("All info")
    info.moduleStates.toSeq.foreach {
      case (mod, state) ⇒ println(f"${mod.module}%-20s $state")
    }
  }
}
