package net.virtualvoid.release.ivy

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
