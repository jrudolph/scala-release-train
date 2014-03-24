package net.virtualvoid.release
package ivy

import model._

case class MissingInfo(missingDependencies: Set[ModuleDef],
                       leafMissing: Set[ModuleDef],
                       maxMissingDepthChain: List[ModuleDef]) {
  require((leafMissing intersect missingDependencies) == leafMissing)
  require((maxMissingDepthChain.toSet intersect missingDependencies) == maxMissingDepthChain.toSet)
  require(maxMissingDepthChain.isEmpty == missingDependencies.isEmpty)

  def merge(other: MissingInfo): MissingInfo =
    MissingInfo(
      missingDependencies union other.missingDependencies,
      leafMissing union other.leafMissing,
      if (maxMissingDepthChain.size >= other.maxMissingDepthChain.size) maxMissingDepthChain else other.maxMissingDepthChain)

  def totalMissing: Int = missingDependencies.size
  def maxDepth: Int = maxMissingDepthChain.size

  def formatChain: String = maxMissingDepthChain.mkString(" -> ")
}
object MissingInfo {
  val empty = MissingInfo(Set.empty, Set.empty, Nil)
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

    def analyzeMissing(lib: Library): MissingInfo = {
      def walk(module: ModuleDef): MissingInfo = {
        info.moduleState(module) match {
          case PreviousVersion(_, _, info) ⇒
            val inner = info.dependencies.map(i ⇒ walk(i.asDef)).reduceOption(_.merge(_)).getOrElse(MissingInfo.empty)
            val leaf = inner.missingDependencies.isEmpty

            MissingInfo(
              inner.missingDependencies + module,
              if (leaf) Set(module) else inner.leafMissing,
              module :: inner.maxMissingDepthChain)
          case _ ⇒ MissingInfo.empty
        }
      }

      walk(lib.moduleDef)
    }

    val missingInfos: Seq[(Library, MissingInfo)] = missing.map(l ⇒ (l, analyzeMissing(l)))
    missingInfos.foreach {
      case (lib, info) ⇒
        import lib._
        println(f"$name%-30s ${info.totalMissing}%2d missing dependencies, deepest chain: ${info.maxDepth}%d ${info.formatChain}")
    }

    println()
    println("Blocking modules")
    println()

    val blocking =
      missingInfos.flatMap(lib ⇒ lib._2.leafMissing.toSeq.map(missing ⇒ (missing, lib._1))).groupBy(_._1).toSeq.map {
        case (mod, occs) ⇒
          (mod, occs.map(_._2))
      }.sortBy(-_._2.size)

    blocking.foreach {
      case (mod, blockedLibraries) ⇒
        println(f"$mod%-50s blocks ${blockedLibraries.size}%2d libraries: ${blockedLibraries.map(_.name).mkString(", ")}")
    }

    /*println()
    println("All info")
    info.moduleStates.toSeq.foreach {
      case (mod, state) ⇒ println(f"${mod.module}%-20s $state")
    }*/
  }
}
