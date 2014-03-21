package net.virtualvoid.release.ivy

import java.io.{ FileWriter, FileOutputStream, OutputStream, File }
import spray.json._
import scala.io.Source
import scala.annotation.tailrec

trait EventStore[E, S] {
  def process(event: E): S
  def state: S
}

trait Storage[T] {
  def store(event: T): Unit
  def readAll: Iterator[T]
}
object Storage {
  def asJsonFromFile[T: RootJsonFormat](f: File): Storage[T] =
    new Storage[T] {
      def readAll: Iterator[T] =
        if (f.exists()) {
          val lines = Source.fromFile(f).getLines()
          lines.map(_.asJson.convertTo[T])
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