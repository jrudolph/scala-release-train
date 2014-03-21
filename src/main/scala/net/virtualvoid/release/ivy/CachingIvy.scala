package net.virtualvoid.release
package ivy

import spray.http.DateTime
import net.virtualvoid.release.model.{ Revision, ModuleInfo, ModuleDef, ModuleID }
import spray.json._

sealed trait Entry {
  def timestamp: DateTime
}
object Entry {
  import spray.json.DefaultJsonProtocol._
  implicit val dateTimeFormat = new JsonFormat[DateTime] {
    def read(json: JsValue): DateTime =
      DateTime.fromIsoDateTimeString(json.convertTo[String]).get

    def write(obj: DateTime): JsValue = obj.toIsoDateTimeString.toJson
  }

  implicit val revisionFormat = jsonFormat1(Revision.apply)
  implicit val moduleIdFormat = jsonFormat3(ModuleID)
  implicit val moduleInfoFormat = jsonFormat3(ModuleInfo)
  implicit val moduleDefFormat = jsonFormat2(ModuleDef)

  implicit val versionsFormat = jsonFormat3(FindVersions)
  implicit val resolutionFormat = jsonFormat3(Resolution)

  implicit val entryFormat = new RootJsonFormat[Entry] {
    def read(json: JsValue): Entry = json match {
      case obj: JsObject if obj.fields("type") == JsString("versions")   ⇒ obj.convertTo[FindVersions]
      case obj: JsObject if obj.fields("type") == JsString("resolution") ⇒ obj.convertTo[Resolution]
    }
    def write(obj: Entry): JsValue = obj match {
      case f: FindVersions ⇒ addType(f.toJson, "versions")
      case r: Resolution   ⇒ addType(r.toJson, "resolution")
    }
    def addType(value: JsValue, tpe: String): JsValue = {
      val obj = value.asJsObject
      obj.copy(fields = obj.fields.updated("type", tpe.toJson))
    }

  }
}
case class FindVersions(timestamp: DateTime, module: ModuleDef, versions: Seq[ModuleID]) extends Entry
case class Resolution(timestamp: DateTime, module: ModuleID, info: ModuleInfo) extends Entry

object CachedIvy {
  case class Cache(versions: Map[ModuleDef, FindVersions] = Map.empty,
                   resolutions: Map[ModuleID, Resolution] = Map.empty)
  def apply(storage: Storage[Entry], backend: IvyInterface): IvyInterface =
    new IvyInterface {
      val store = EventStore(storage, Cache()) { (state, event) ⇒
        event match {
          case f: FindVersions ⇒ state.copy(versions = state.versions.updated(f.module, f))
          case r: Resolution   ⇒ state.copy(resolutions = state.resolutions.updated(r.module, r))
        }
      }
      def get[K, V <: Entry](extract: Cache ⇒ Option[V])(produce: ⇒ V): V =
        extract(store.state) match {
          case Some(v) ⇒ v
          case None ⇒
            val v = produce
            store.process(v)
            v
        }

      def findJavaVersion(module: ModuleDef): Seq[ModuleID] =
        get(_.versions.get(module))(FindVersions(DateTime.now, module, backend.findJavaVersion(module))).versions
      def resolve(module: ModuleID): ModuleInfo =
        get(_.resolutions.get(module))(Resolution(DateTime.now, module, backend.resolve(module))).info
    }
}
