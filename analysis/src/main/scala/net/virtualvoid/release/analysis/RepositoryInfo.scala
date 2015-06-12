package net.virtualvoid.release.analysis

import net.virtualvoid.release.model.{ ModuleDef, Library, ScalaVersion, ModuleID }
import net.virtualvoid.release.{ RepositoryInterface, model }
import scala.annotation.tailrec

sealed trait ModuleState {
  def isAvailable: Boolean
}
case class HasTargetVersion(versions: Seq[ModuleID]) extends ModuleState {
  def isAvailable = true
}
case class PreviousVersion(versions: Seq[ModuleID], latest: ModuleID, latestInfo: model.ModuleInfo) extends ModuleState {
  def isAvailable = false
}
case object PreviouslyUnknown extends ModuleState {
  def isAvailable: Boolean = false
}
case class OtherDependency(versions: Seq[ModuleID]) extends ModuleState {
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
  def gather(ivy: RepositoryInterface, _libraries: Seq[Library], _targetVersion: ScalaVersion, _lastVersion: ScalaVersion, quiet: Boolean = false): RepositoryInfo = {
    val libraryModules: Seq[ModuleDef] = _libraries.map(_.moduleDef)

    def resolve(module: ModuleDef): ModuleState = {
      if (!quiet) Console.err.println(s"Resolving $module")
      val lastVersions = ivy.findVersion(module, _lastVersion)
      // This will prevent modules that was a java/other module in lastVersion to change being a Scala module
      // in targetVersion if not declared as a library
      if (lastVersions.isEmpty && !_libraries.exists(_.moduleDef == module)) OtherDependency(ivy.findJavaVersion(module))
      else {
        val versions = ivy.findVersion(module, _targetVersion)
        if (versions.nonEmpty) HasTargetVersion(versions)
        else if (lastVersions.isEmpty) PreviouslyUnknown
        else {
          val latest = lastVersions.maxBy(_.revision)
          val info = ivy.resolve(latest)
          assert(info.scalaVersion.isDefined)
          PreviousVersion(lastVersions, latest, info)
        }
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
          //case OtherDependency =>
          case _ ⇒ throw new IllegalStateException(s"Cannot find state for $module (for scala $scalaVersion)")
        }
    }
  }
}
