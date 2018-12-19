package interpreters

import algebra.Messages.{NoteOff, NoteOn}
import algebra.types.{Msg, ToScore}
import algebra.{Device, MidiApi, Note, Receiver}
import cats.effect.Timer
import cats.implicits._
import cats.{MonadError, Show}
import javax.sound.midi.MidiDevice

class DeviceInterpreter[F[_]](val device: MidiDevice, api: MidiApi[F], timer: Timer[F])(
    implicit F: MonadError[F, Throwable])
    extends Device[F] {
  private val receiver: F[Receiver[F]] = api.receiver(device)

  override def send[T: Msg: Show](value: T): F[Unit] = receiver >>= (_.send(value, 0))

  override def bleep[T](value: T)(implicit toScore: ToScore[T]): F[Unit] =
    toScore(value).traverse {
      case Left(Note(i, b)) ⇒
        send(NoteOn(i)) >> timer.sleep(b) >> send(NoteOff(i))
      case Right(d) ⇒
        timer.sleep(d)
    }.void

  def close(): F[Unit] = F.catchNonFatal(device.close())
  def open(): F[Unit]  = F.catchNonFatal(device.open())
}
