package net.virtualvoid

import net.virtualvoid.release.model.Revision

package object release {
  implicit class EnrichRevisionSeq(val revs: Seq[Revision]) extends AnyVal {
    def latest: Option[Revision] =
      if (revs.isEmpty) None
      else Some(revs.max)
  }
}
