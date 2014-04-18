package net.virtualvoid.release
package ivy

import model._

case class MissingInfo(missingDependencies: Set[ModuleDef],
                       leafMissing: Set[ModuleDef],
                       maxDepthMissingChain: List[ModuleDef]) {
  require((leafMissing intersect missingDependencies) == leafMissing)
  require((maxDepthMissingChain.toSet intersect missingDependencies) == maxDepthMissingChain.toSet)
  require(maxDepthMissingChain.isEmpty == missingDependencies.isEmpty)

  def merge(other: MissingInfo): MissingInfo =
    MissingInfo(
      missingDependencies union other.missingDependencies,
      leafMissing union other.leafMissing,
      if (maxDepthMissingChain.size >= other.maxDepthMissingChain.size) maxDepthMissingChain else other.maxDepthMissingChain)

  def totalMissing: Int = missingDependencies.size - 1
  def maxDepth: Int = maxDepthMissingChain.size - 1

  def formatChain: String = maxDepthMissingChain.drop(1).mkString(" -> ")
}
object MissingInfo {
  val empty = MissingInfo(Set.empty, Set.empty, Nil)
}

object Analysis {
  val MaxVersions = 5

  def simpleMissingDependencyAnalysis(info: RepositoryInfo): Unit = {
    import ExtraMethods._

    val (available, missing) = info.libraries.partition(l ⇒ info.isAvailable(l.moduleDef))

    println(f"${available.size}%2d libraries available for ${info.targetVersion}%s (see the end for sbt config lines)")
    println()
    available.foreach { lib ⇒
      import lib._
      val versions = info.modulesForScalaVersion(lib.moduleDef, info.targetVersion).map(_.revision).sortBy(_.toString).reverse
      val versionsString = versions.take(MaxVersions).mkString(", ")
      val ellipsis = if (versions.size > MaxVersions) s", ... [${versions.size - MaxVersions} more]" else ""
      println(f"$name%-30s ${versions.size}%3d versions: $versionsString$ellipsis")
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
              module :: inner.maxDepthMissingChain)
          case _ ⇒ MissingInfo.empty
        }
      }

      walk(lib.moduleDef)
    }

    val missingInfos: Seq[(Library, MissingInfo)] = missing.map(l ⇒ (l, analyzeMissing(l))).sortBy(_._2.totalMissing)
    missingInfos.foreach {
      case (lib, info) ⇒
        import lib._

        if (info.totalMissing >= 0)
          println(f"$name%-30s ${info.totalMissing}%2d missing dependencies, deepest chain: ${info.maxDepth}%d ${info.formatChain}")
        else // previously unknown
          println(f"$name%-30s has no previous version to analyze")
    }

    println()
    println("Blocking modules")
    println()

    val blocking: Seq[(ModuleDef, Seq[Library])] =
      missingInfos.flatMap(lib ⇒ lib._2.leafMissing.toSeq.map(missing ⇒ (missing, lib._1))).groupBy(_._1).toSeq.map {
        case (mod, occs) ⇒
          (mod, occs.map(_._2).filterNot(_.moduleDef == mod))
      }.sortBy(e ⇒ (-e._2.size, e._1.organization))

    blocking.foreach {
      case (mod, blockedLibraries) ⇒
        if (blockedLibraries.nonEmpty)
          println(f"$mod%-50s blocks ${blockedLibraries.size}%2d libraries: ${blockedLibraries.map(_.name).mkString(", ")}")
        else println(f"$mod%-50s blocks nothing but itself")
    }

    println()
    println("Sbt config lines for all available libraries in alphabetic order")
    println()
    for {
      lib ← available.sortBy(l ⇒ (l.organization, l.module))
      rev ← info.modulesForScalaVersion(lib.moduleDef, info.targetVersion).map(_.revision).sorted
    } {
      import lib._
      println(s""""$organization" %% "$module" % "$rev"""")
    }

    /*println()
    println("All info")
    info.moduleStates.toSeq.foreach {
      case (mod, state) ⇒ println(f"${mod.module}%-20s $state")
    }*/
  }
}
