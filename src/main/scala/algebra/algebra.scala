package algebra
import cats.Show
import cats.effect.Resource
import javax.sound.midi.MidiDevice.Info
import javax.sound.midi.{Instrument, MidiDevice, MidiMessage}

import scala.concurrent.duration.FiniteDuration

object algebra {
  //type Bite = Int Refined Closed[W.`0`.T, W.`127`.T]

  trait Devices[F[_]] {
    def open(name: String): Resource[F, Device[F]]
  }

  trait Receiver[F[_]] {
    def send[T](t: T, i: Long)(implicit msg: Msg[T], show: Show[T]): F[Unit]
  }

  trait Device[F[_]] {
    def send[T: Msg: Show](msg: T): F[Unit]
    def bleep(i: Int, d: FiniteDuration): F[Unit]
  }

  type Msg[T] = T ⇒ MidiMessage

  trait MidiApi[F[_]] {
    def midiDevice(info: Info): F[MidiDevice]
    def receiver(device: MidiDevice): F[Receiver[F]]
    def midiDeviceInfo: F[List[Info]]
    def instruments: F[List[Instrument]]
  }
}
