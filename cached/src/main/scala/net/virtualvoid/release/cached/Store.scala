package net.virtualvoid.release
package cached

import java.io.{ FileWriter, File }
import spray.json._
import scala.io.Source

trait EventStore[E, S] {
  def process(event: E): S
  def state: S
}

trait Storage[T] {
  def store(event: T): Unit
  def readAll: Iterator[T]
}
object Storage {
  def asJsonFromFile[T: RootJsonFormat](f: File, isOld: T ⇒ Boolean = (_: T) ⇒ false): Storage[T] =
    new Storage[T] {
      def readAll: Iterator[T] =
        if (f.exists()) {
          val lines = Source.fromFile(f).getLines()
          lines.map(_.parseJson.convertTo[T]).filterNot(isOld)
        } else Iterator.empty

      val writer = new FileWriter(f, true)
      def store(event: T): Unit = {
        writer.append(event.toJson.compactPrint)
        writer.append("\n")
        writer.flush()
      }
    }
}

object EventStore {
  type Updater[E, S] = (S, E) ⇒ S

  def apply[E, S](storage: Storage[E], initialState: S)(f: Updater[E, S]): EventStore[E, S] =
    new EventStore[E, S] {
      var state = storage.readAll.foldLeft(initialState)(f)
      def process(event: E): S = {
        storage.store(event)
        state = f(state, event)
        state
      }
    }
}