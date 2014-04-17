package net.virtualvoid.release
package ivy

import model.{ Library, ScalaVersion }
import java.io.File
import spray.http.DateTime
import scala.concurrent.duration._
import sbt.{ Level, LogEvent, ControlEvent }

object Main extends App {
  val targetVersion = ScalaVersion.`2.11`
  val lastVersion = ScalaVersion.`2.10`

  implicit class AddIsOlderThan(val timestamp: DateTime) extends AnyVal {
    def isOlderThan(duration: Duration): Boolean =
      (DateTime.now.clicks - timestamp.clicks) > duration.toMillis
  }

  def isOldVersions(e: Entry): Boolean = e match {
    case FindVersions(timestamp, mod, versions) ⇒
      mod.module.endsWith(targetVersion.version) &&
        ((versions.isEmpty && timestamp.isOlderThan(1.hour)) ||
          (versions.nonEmpty && timestamp.isOlderThan(1.day)))
    case Resolution(timestamp, _, _) ⇒ timestamp.isOlderThan(3.days)
    case _                           ⇒ false
  }

  val quiet = args.exists(_ == "-q")
  val logger = if (quiet) NoLogger else sbt.ConsoleLogger()
  val storage = Storage.asJsonFromFile[Entry](new File("cache.bin"), isOldVersions)
  val impl = CachedIvy(storage, new IvyImplementation(logger))
  import ExtraMethods._

  def report(lib: Library): Unit = {
    val versions = impl.findVersion(lib, targetVersion)
    import lib._
    println(f"$name%-30s ${versions.size}%3d versions${versions.map(_.revision).latest.fold("")(v ⇒ s", latest: $v")}")
  }
  def reportLastVersion(lib: Library): Unit = {
    import lib._
    val versions = impl.findVersion(lib, lastVersion)
    println(f"$name%-30s ${versions.size}%3d versions${versions.map(_.revision).latest.fold("")(v ⇒ s", latest: $v")}")
    val info = impl.resolve(versions.maxBy(_.revision))
    println(info, info.scalaVersion)
  }

  val selected = Seq(Libraries.scalacheck, Libraries.scalaTest, Libraries.sprayJson)
  val info = RepositoryInfo.gather(impl, Libraries.all, targetVersion, lastVersion, quiet)
  Analysis.simpleMissingDependencyAnalysis(info)
  //Libraries.all.foreach(report)
}

object NoLogger extends sbt.BasicLogger {
  def control(event: ControlEvent.Value, message: ⇒ String): Unit = {}

  def log(level: Level.Value, message: ⇒ String): Unit = {}
  def success(message: ⇒ String): Unit = {}
  def trace(t: ⇒ Throwable): Unit = {}
  def logAll(events: Seq[LogEvent]): Unit = {}
}