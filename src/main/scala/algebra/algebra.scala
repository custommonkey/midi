package algebra

import algebra.types.{Report, ToScore}
import cats.Show
import cats.effect.Resource
import javax.sound.midi.MidiDevice.Info
import javax.sound.midi.{Instrument, MidiDevice, MidiMessage}

import scala.concurrent.duration.FiniteDuration

object types {
  type Msg[T]     = T ⇒ MidiMessage
  type Report[T]  = T ⇒ String
  type Thing      = Either[Note, FiniteDuration]
  type Score      = List[Thing]
  type ToScore[T] = T ⇒ Score

  object Msg {
    def apply[T](implicit instance: Msg[T]): Msg[T] = instance
  }
  object Thing {
    def apply(n: Note): Thing           = Left(n)
    def apply(n: FiniteDuration): Thing = Right(n)
  }
}

import algebra.types.Msg

case class Note(n: Int, d: FiniteDuration)

case class DeviceDef(name: String)

trait Devices[F[_]] {
  def open(name: DeviceDef): Resource[F, Device[F]]
}

trait Receiver[F[_]] {
  def send[T](t: T, i: Long)(implicit msg: Msg[T], show: Show[T]): F[Unit]
}

trait Device[F[_]] {
  def send[T: Msg: Show](msg: T): F[Unit]
//  def randomProgram: F[Unit]
  def bleep[T: ToScore](t: T): F[Unit]
}

trait MidiApi[F[_]] {
  def midiDevice(info: Info): F[MidiDevice]
  def receiver(device: MidiDevice): F[Receiver[F]]
  def midiDeviceInfo: F[List[Info]]
  def instruments: F[List[Instrument]]
}

trait Reports[F[_]] {
  def report[E: Report](e: E): F[Unit]
}

trait Print[F[_]] {
  def apply[T](value: T)(implicit show: Show[T]): F[Unit]
}

trait Utils[F[_]] {
  def showInstruments: F[Unit]
  def randomProgram(device: Device[F]): F[Unit]
}

trait RandomApi[F[_]] {
  def apply[T](implicit random: Random[T]): F[T]
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
