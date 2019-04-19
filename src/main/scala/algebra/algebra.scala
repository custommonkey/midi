package algebra

import algebra.types.{Channel, MidiInt, Msg, Score}
import cats.Show
import cats.effect.Resource
import eu.timepit.refined.W
import eu.timepit.refined.api.RefType.applyRef
import eu.timepit.refined.api.Refined
import eu.timepit.refined.auto._
import eu.timepit.refined.numeric.Interval.Closed
import eu.timepit.refined.numeric.NonNegative
import javax.sound.midi.MidiDevice.Info
import javax.sound.midi.{Instrument, MidiDevice, MidiMessage}

import scala.concurrent.duration.{FiniteDuration, _}
import scala.util.Random.nextInt
import Events.Events

object types {
  type Msg[T]      = T => MidiMessage
  type Score       = List[NoteOrRest]
  type Channel     = Int Refined Closed[W.`0`.T, W.`15`.T]
  type MidiInt     = Int Refined Closed[W.`1`.T, W.`128`.T]
  type Timestamp   = Long Refined NonNegative
  type Sleep[F[_]] = FiniteDuration => F[Unit]

  object MidiInt {

    val Max: MidiInt = 128

    implicit val randomNint: Random[MidiInt] = () => applyRef[MidiInt].unsafeFrom(nextInt(127) + 1)

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
    implicit val randomTimestamp: Random[Timestamp] = () =>
      applyRef[Timestamp].unsafeFrom(scala.util.Random.nextLong() + 1)
    implicit class TimestampOps(n: Timestamp) {
      private def op(f: Long => Long) =
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
  def +(n: NoteOrRest): Score = List(this, n)
}

final case class Note(n: MidiInt, duration: FiniteDuration) extends NoteOrRest
final case class Rest(duration: FiniteDuration)             extends NoteOrRest

case class DeviceDef(name: String)

sealed trait EvType
case object On  extends EvType
case object Off extends EvType

trait Devices[F[_]] {
  def open(name: DeviceDef): Resource[F, Device[F]]
}

trait Receiver[F[_]] {
  def apply[T: Msg](t: T, i: Long): F[Unit]
}

trait Device[F[_]] {
  def apply[T: Msg](msg: T): F[Unit]
  def apply(events: Events[Event[FiniteDuration]])(implicit channel: Channel): F[Unit]
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
  def apply[T](value: T)(implicit s: Show[T]): F[Unit]
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
object Random {
  implicit def randomList[T](implicit random: Random[T]): Random[List[T]] =
    () => List.fill(10)(random())
}

trait Algebra[F[_]] {
  def println: Print[F]
  def reports: Reports[F]
  def api: MidiApi[F]
  def devices: Devices[F]
  def utils: Utils[F]
  def random: RandomApi[F]
}
