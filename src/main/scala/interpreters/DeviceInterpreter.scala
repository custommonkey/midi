package interpreters

import algebra.Messages.{NoteOff, NoteOn}
import algebra.algebra._
import cats.effect.Timer
import cats.implicits._
import cats.{MonadError, Show}
import javax.sound.midi.MidiDevice

import scala.concurrent.duration.FiniteDuration

class DeviceInterpreter[F[_]](val device: MidiDevice, api: MidiApi[F], timer: Timer[F])(
    implicit F: MonadError[F, Throwable])
    extends Device[F] {
  private val receiver: F[Receiver[F]] = api.receiver(device)

  override def send[T: Msg: Show](value: T): F[Unit] =
    receiver.flatMap(_.send(value, 0))

  def bleep(i: Int, d: FiniteDuration): F[Unit] =
    send(NoteOn(i)) >> timer.sleep(d) >> send(NoteOff(i))

  def close(): F[Unit] = F.catchNonFatal(device.close())
  def open(): F[Unit] = F.catchNonFatal(device.open())
}
