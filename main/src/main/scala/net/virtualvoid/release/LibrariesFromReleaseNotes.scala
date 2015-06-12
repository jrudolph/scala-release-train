package net.virtualvoid.release

import net.virtualvoid.release.model.{ ModuleDef, Library }
import scala.io.Source
import net.virtualvoid.release.analysis.{ HasTargetVersion, RepositoryInfo }

object LibrariesFromReleaseNotes {
  //val LibraryEntry = """\s*"([^"]+)"\s*%%\s*"([^"]+)"\s*%\s*([^"]+)\s*""".r
  val LibraryEntry = """\s*"([^"]+)"\s*%%\s*"([^"]+)".*""".r

  def loadLibraries(fileName: String): Seq[Library] =
    Source.fromFile(fileName).getLines().toSeq.map {
      case LibraryEntry(org, name) ⇒ Library(name, ModuleDef(org, name), None)
      case str                     ⇒ throw new IllegalStateException(s"Invalid entry '$str'")
    }

  def report(info: RepositoryInfo): Unit = {
    val tuples =
      info.libraries.filter(lib ⇒ info.isAvailable(lib.moduleDef)).map { lib ⇒
        info.moduleState(lib.moduleDef) match {
          case HasTargetVersion(versions) ⇒
            val latest = versions.map(_.revision).max
            (lib.organization, lib.name, latest)
          case _ ⇒ throw new IllegalStateException("All modules in list should have a candidate version")
        }
      }

    val maxOrgChars = tuples.map(_._1.length).max + 2
    val maxNameChars = tuples.map(_._2.length).max //+ 2
    def quoted(str: String): String = '"' + str + '"'
    //val formatString = s"""    %-${maxOrgChars}s %%%% %-${maxNameChars}s %% "%s""""
    val formatString = s"""%s %s %s"""

    tuples.map(t ⇒ formatString.format(t._1, t._2, t._3)).foreach(println)
  }
}

