package net.virtualvoid.release.ivy

import sbt.{ LogEvent, Level, ControlEvent }

object NoLogger extends sbt.BasicLogger {
  def control(event: ControlEvent.Value, message: ⇒ String): Unit = {}

  def log(level: Level.Value, message: ⇒ String): Unit = {}
  def success(message: ⇒ String): Unit = {}
  def trace(t: ⇒ Throwable): Unit = {}
  def logAll(events: Seq[LogEvent]): Unit = {}
}