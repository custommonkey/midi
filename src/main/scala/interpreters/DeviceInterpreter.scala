package interpreters

import algebra.Messages.{NoteOff, NoteOn}
import algebra.types.{Channel, Msg, Sleep}
import algebra.{Device, Event, Events, MidiApi, Off, On, Receiver}
import cats.MonadError
import cats.implicits._
import javax.sound.midi.MidiDevice

import scala.concurrent.duration.FiniteDuration

class DeviceInterpreter[F[_]](val device: MidiDevice, api: MidiApi[F], sleep: Sleep[F])(
    implicit F: MonadError[F, Throwable])
    extends Device[F] {

  private val receiver: F[Receiver[F]] = api.receiver(device)

  override def apply[T: Msg](value: T): F[Unit] = receiver >>= (_(value, 0))

  override def apply(events: Events[FiniteDuration])(implicit channel: Channel): F[Unit] =
    events.events.traverse {
      case Event(On, n, timestamp)  ⇒ sleep(timestamp) >> apply(NoteOn(n, channel))
      case Event(Off, n, timestamp) ⇒ sleep(timestamp) >> apply(NoteOff(n, channel))
    }.void

  def close: F[Unit] = F.catchNonFatal(device.close())
  def open: F[Unit]  = F.catchNonFatal(device.open())
}
