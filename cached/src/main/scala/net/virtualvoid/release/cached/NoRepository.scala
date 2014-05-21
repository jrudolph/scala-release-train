package net.virtualvoid.release.cached

import net.virtualvoid.release.RepositoryInterface
import net.virtualvoid.release.model.{ ModuleInfo, ModuleID, ModuleDef }

object NoRepository extends RepositoryInterface {
  def resolve(module: ModuleID): ModuleInfo = fail()
  def findJavaVersion(module: ModuleDef): Seq[ModuleID] = fail()

  def fail(): Nothing = throw new IllegalStateException("No backend defined")
}
