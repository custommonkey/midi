package algebra

import algebra.types.{Channel, Nint, Score}
import cats.Show
import cats.effect.Resource
import eu.timepit.refined.W
import eu.timepit.refined.api.{RefType, Refined}
import eu.timepit.refined.numeric.Interval.Closed
import eu.timepit.refined.numeric.{NonNegative, Positive}
import javax.sound.midi.MidiDevice.Info
import javax.sound.midi.{Instrument, MidiDevice, MidiMessage}

import scala.concurrent.duration.FiniteDuration

object types {
  type Msg[T]    = T ⇒ MidiMessage
  type Score     = List[NoteOrRest]
  type Channel   = Int Refined Closed[W.`0`.T, W.`15`.T]
  type Nint      = Int Refined Positive
  type Timestamp = Int Refined NonNegative

  object Nint {
    implicit val randomNint: Random[Nint] = () ⇒
      RefType.applyRef[Nint].unsafeFrom(scala.util.Random.nextInt() + 1)
    def +(n: Nint, i: Int): Nint = RefType.applyRef[Nint].unsafeFrom(n.value + i)
  }
  object Timestamp {
    implicit val randomTimestamp: Random[Timestamp] = () ⇒
      RefType.applyRef[Timestamp].unsafeFrom(scala.util.Random.nextInt() + 1)
    implicit class TimestampOps(n: Timestamp) {
      def +(i: FiniteDuration): Timestamp =
        RefType.applyRef[Timestamp].unsafeFrom(n.value + i.toMillis.toInt)
      def +(i: Timestamp): Timestamp =
        RefType.applyRef[Timestamp].unsafeFrom(n.value + i.value)
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

final case class Note(n: Nint, duration: FiniteDuration) extends NoteOrRest
final case class Rest(duration: FiniteDuration)          extends NoteOrRest

import algebra.types.Msg

case class DeviceDef(name: String)

trait Devices[F[_]] {
  def open(name: DeviceDef): Resource[F, Device[F]]
}

trait Receiver[F[_]] {
  def send[T: Msg: Show](t: T, i: Long): F[Unit]
}

trait Device[F[_]] {
  def send[T: Msg: Show](msg: T): F[Unit]
//  def randomProgram: F[Unit]
  def <<[T](t: T)(implicit c: Channel, s: ToScore[T]): F[Unit]
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
