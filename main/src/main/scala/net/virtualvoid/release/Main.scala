package net.virtualvoid.release

import model.ScalaVersion
import java.io.File
import spray.http.DateTime
import scala.concurrent.duration._
import cached._
import analysis.{ Analysis, RepositoryInfo }
import ivy.IvyRepositoryImplementation

object Main extends App {
  val targetVersion = ScalaVersion.`2.11`
  val lastVersion = ScalaVersion.`2.10`

  val quiet = args.exists(_ == "-q")
  val preferCached = args.exists(_ == "-c")
  val onlyCached = args.exists(_ == "-n")
  def maxCachedFor(dur: Duration): Duration = if (preferCached || onlyCached) 10000.days else dur

  val maxTargetVersionMissingAge = maxCachedFor(10.minutes)
  val maxTargetVersionExistingAge = maxCachedFor(6.hours)
  val maxOldVersionAge = maxCachedFor(1.day)

  implicit class AddIsOlderThan(val timestamp: DateTime) extends AnyVal {
    def isOlderThan(duration: Duration): Boolean =
      (DateTime.now.clicks - timestamp.clicks) > duration.toMillis
  }

  def isOldVersions(e: Entry): Boolean = e match {
    case FindVersions(timestamp, mod, versions) ⇒
      mod.module.endsWith(targetVersion.version) &&
        ((versions.isEmpty && timestamp.isOlderThan(maxTargetVersionMissingAge)) ||
          (versions.nonEmpty && timestamp.isOlderThan(maxTargetVersionExistingAge)))
    case Resolution(timestamp, _, _) ⇒ timestamp.isOlderThan(maxOldVersionAge)
    case _                           ⇒ false
  }

  val storage = Storage.asJsonFromFile[Entry](new File("cache.bin"), isOldVersions)
  val backend = if (onlyCached) NoRepository else IvyRepositoryImplementation(quiet)
  val impl = CachedRepository(storage, backend)

  val info = RepositoryInfo.gather(impl, Libraries.all, targetVersion, lastVersion, quiet)
  Analysis.simpleMissingDependencyAnalysis(info)

  val Disclaimer =
    """
      |Disclaimer: This tool only regards "test" dependencies if they are part of the pom.
      |It contains a fixed list of libraries and will only detect any releases for those
      |(or their transitive dependencies).
      |
      |These libraries are currently checked (name, organization % module):
    """.stripMargin
  println(Disclaimer)
  Libraries.all.sortBy(_.name).foreach { lib ⇒
    import lib._
    println(f"$name%-25s $moduleDef")
  }
}
