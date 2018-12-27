package algebra

import algebra.types.{Channel, MidiInt, Score}
import cats.effect.Resource
import cats.{Functor, Monoid, Show}
import eu.timepit.refined.W
import eu.timepit.refined.api.RefType.applyRef
import eu.timepit.refined.api.Refined
import eu.timepit.refined.auto._
import eu.timepit.refined.numeric.Interval.Closed
import eu.timepit.refined.numeric.NonNegative
import javax.sound.midi.MidiDevice.Info
import javax.sound.midi.{Instrument, MidiDevice, MidiMessage}
import cats.implicits._
import algebra.types.Msg

import scala.concurrent.duration.{FiniteDuration, _}
import scala.util.Random.nextInt

object types {
  type Msg[T]    = T ⇒ MidiMessage
  type Score     = List[NoteOrRest]
  type Channel   = Int Refined Closed[W.`0`.T, W.`15`.T]
  type MidiInt   = Int Refined Closed[W.`1`.T, W.`128`.T]
  type Timestamp = Long Refined NonNegative

  type Time = Long

  object MidiInt {
    implicit val randomNint: Random[MidiInt] = () ⇒ applyRef[MidiInt].unsafeFrom(nextInt(127) + 1)
    val Max: MidiInt                         = 128
    implicit class MidiIntOps(m: MidiInt) {
      def +(i: MidiInt): MidiInt = {
        val x = m.value + i.value
        if (x > MidiInt.Max) {
          MidiInt.Max
        } else {
          applyRef[MidiInt].unsafeFrom(m.value + i.value)
        }
      }
    }
  }
  object Timestamp {
    implicit val randomTimestamp: Random[Timestamp] = () ⇒
      applyRef[Timestamp].unsafeFrom(scala.util.Random.nextLong() + 1)
    implicit class TimestampOps(n: Timestamp) {
      private def op(f: Long ⇒ Long) =
        applyRef[Timestamp].unsafeFrom(f(n.value))
      def +(i: FiniteDuration): Timestamp = op(_ + i.toMillis.toInt)
      def +(i: Timestamp): Timestamp      = op(_ + i.value)
      def -(i: Timestamp): FiniteDuration = op(_ - i.value).value.millis
    }
  }

  object Msg {
    def apply[T](implicit instance: Msg[T]): Msg[T] = instance
  }
}

trait ToScore[T] {
  def apply(t: T): Score
}

sealed trait NoteOrRest {
  def duration: FiniteDuration
}

final case class Note(n: MidiInt, duration: FiniteDuration) extends NoteOrRest
final case class Rest(duration: FiniteDuration)             extends NoteOrRest

case class DeviceDef(name: String)

sealed trait EvType
case object On  extends EvType
case object Off extends EvType

case class Event[T](e: EvType, n: MidiInt, t: T)
object Event {
  implicit val functorEvent: Functor[Event] = new Functor[Event] {
    override def map[A, B](fa: Event[A])(f: A ⇒ B): Event[B] = fa match {
      case Event(evType, midiInt, t) ⇒ Event(evType, midiInt, f(t))
    }
  }

}
case class Events[T](events: List[Event[T]]) {
  def +(e: Events[T]): Events[T] = this |+| e
}

object Events {
  def apply[T](events: Event[T]*): Events[T] = new Events(events.to)
  implicit def monoidEvents[F[_], T]: Monoid[Events[T]] = new Monoid[Events[T]] {
    override def empty: Events[T]                               = Events(List())
    override def combine(x: Events[T], y: Events[T]): Events[T] = Events(x.events ::: y.events)
  }
  implicit val functorEvents: Functor[Events] = new Functor[Events] {
    override def map[A, B](fa: Events[A])(f: A ⇒ B): Events[B] = Events(fa.events.map(_.map(f)))
  }
}

trait Devices[F[_]] {
  def open(name: DeviceDef): Resource[F, Device[F]]
}

trait Receiver[F[_]] {
  def apply[T: Msg: Show](t: T, i: Long): F[Unit]
}

trait Device[F[_]] {
  def send[T: Msg: Show](msg: T): F[Unit]
  def <<(events: Events[FiniteDuration])(implicit channel: Channel): F[Unit]
}

trait MidiApi[F[_]] {
  def midiDevice(info: Info): F[MidiDevice]
  def receiver(device: MidiDevice): F[Receiver[F]]
  def midiDeviceInfo: F[List[Info]]
  def instruments: F[List[Instrument]]
}

trait Reports[F[_]] {
  def report[E: Show](e: E): F[Unit]
}

trait Print[F[_]] {
  def apply[T: Show](value: T): F[Unit]
}

trait Utils[F[_]] {
  def showInstruments: F[Unit]
  def showDevices: F[Unit]
  def randomProgram(device: Device[F]): F[Unit]
}

trait RandomApi[F[_]] {
  def apply[T](implicit r: Random[T]): F[T]
}

trait Random[T] {
  def apply(): T
}

trait Algebra[F[_]] {
  def println: Print[F]
  def reports: Reports[F]
  def api: MidiApi[F]
  def devices: Devices[F]
  def utils: Utils[F]
  def random: RandomApi[F]
}
