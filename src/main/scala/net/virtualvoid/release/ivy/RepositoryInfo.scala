package net.virtualvoid.release.ivy

import net.virtualvoid.release.model.{ ModuleDef, Library, ScalaVersion, ModuleID }
import net.virtualvoid.release.model
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
        } else OtherDependency(ivy.findJavaVersion(module))
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
