package algebra

import algebra.types.{Channel, ToScore}
import cats.Show
import cats.effect.Resource
import eu.timepit.refined.W
import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.Interval.Closed
import javax.sound.midi.MidiDevice.Info
import javax.sound.midi.{Instrument, MidiDevice, MidiMessage}

import scala.concurrent.duration.FiniteDuration

object types {
  type Msg[T]     = T ⇒ MidiMessage
  type Thing      = Either[Note, FiniteDuration]
  type Score      = List[Thing]
  type ToScore[T] = T ⇒ Score
  type Channel    = Int Refined Closed[W.`0`.T, W.`15`.T]
  type Note       = (Int, FiniteDuration)

  object Msg {
    def apply[T](implicit instance: Msg[T]): Msg[T] = instance
  }
  object Thing {
    def apply(n: Note): Thing           = Left(n)
    def apply(n: FiniteDuration): Thing = Right(n)
  }
}

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
  def apply[T: Random]: F[T]
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
