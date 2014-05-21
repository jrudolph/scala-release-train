package net.virtualvoid.release

import net.virtualvoid.release.model.{ ModuleDef, ModuleID, ScalaVersion, Library }

trait RepositoryInterface {
  def findVersion(library: Library, scalaVersion: ScalaVersion): Seq[ModuleID] =
    findVersion(library.organization, library.module, scalaVersion)
  def findVersion(module: ModuleDef, scalaVersion: ScalaVersion): Seq[ModuleID] =
    findVersion(module.organization, module.module, scalaVersion)
  def findVersion(organization: String, module: String, scalaVersion: ScalaVersion): Seq[ModuleID] =
    findJavaVersion(ModuleDef(organization, crossVersionModule(organization, module, scalaVersion.version)))

  def crossVersionModule(organization: String, module: String, version: String): String =
    if (module.startsWith("spray") && !module.endsWith("json") && version == "2.10") module
    //else if (organization.startsWith("org.scala-lang") && version == "2.10") module
    else s"${module}_$version"

  def findJavaVersion(module: ModuleDef): Seq[ModuleID]
  def resolve(module: ModuleID): model.ModuleInfo
}

