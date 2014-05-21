package net.virtualvoid.release
package ivy

import model.{ Library, ScalaVersion }
import java.io.File
import spray.http.DateTime
import scala.concurrent.duration._
import sbt.{ Level, LogEvent, ControlEvent }
import net.virtualvoid.release.cached._
import net.virtualvoid.release.cached.FindVersions
import net.virtualvoid.release.analysis.{ Analysis, RepositoryInfo }

object Main extends App {
  val targetVersion = ScalaVersion.`2.11`
  val lastVersion = ScalaVersion.`2.10`

  val quiet = args.exists(_ == "-q")
  val onlyCached = args.exists(_ == "-c")
  def maxCachedFor(dur: Duration): Duration = if (onlyCached) 1000.days else dur

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

  val logger = if (quiet) NoLogger else sbt.ConsoleLogger()
  val storage = Storage.asJsonFromFile[Entry](new File("cache.bin"), isOldVersions)
  val impl = CachedRepository(storage, new IvyRepositoryImplementation(logger))

  val selected = Seq(Libraries.scalacheck, Libraries.scalaTest, Libraries.sprayJson)
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

object NoLogger extends sbt.BasicLogger {
  def control(event: ControlEvent.Value, message: ⇒ String): Unit = {}

  def log(level: Level.Value, message: ⇒ String): Unit = {}
  def success(message: ⇒ String): Unit = {}
  def trace(t: ⇒ Throwable): Unit = {}
  def logAll(events: Seq[LogEvent]): Unit = {}
}