package net.virtualvoid.release.cached

import net.virtualvoid.release.RepositoryInterface
import net.virtualvoid.release.model.{ ModuleInfo, ModuleID, ModuleDef }

object NoRepository extends RepositoryInterface {
  def resolve(module: ModuleID): ModuleInfo = fail(s"resolve $module")
  def findJavaVersion(module: ModuleDef): Seq[ModuleID] = fail(s"findJavaVersion of $module")

  def fail(msg: String): Nothing = throw new IllegalStateException(s"No backend defined to $msg")
}
