package interpreters

import algebra.Messages.{NoteOff, NoteOn}
import algebra.types.{Channel, Msg}
import algebra.{Device, Event, Events, MidiApi, Off, On, Print, Receiver}
import cats.effect.Timer
import cats.implicits._
import cats.{MonadError, Show}
import javax.sound.midi.MidiDevice
import Console.GREEN
import Console.YELLOW
import Console.RESET

import scala.concurrent.duration.FiniteDuration

class DeviceInterpreter[F[_]](val device: MidiDevice,
                              api: MidiApi[F],
                              timer: Timer[F],
                              println: Print[F])(implicit F: MonadError[F, Throwable])
    extends Device[F] {

  private val receiver: F[Receiver[F]] = api.receiver(device)

  override def send[T: Msg: Show](value: T): F[Unit] = receiver >>= (_(value, 0))

  override def <<(events: Events[FiniteDuration])(implicit channel: Channel): F[Unit] =
    events.events.traverse {
      case Event(On, n, timestamp) ⇒
        println(s"$GREEN$n:$channel:$timestamp$RESET") >>
          timer.sleep(timestamp) >>
          send(NoteOn(n, channel))
      case Event(Off, n, timestamp) ⇒
        println(s"$YELLOW$n:$channel:$timestamp$RESET") >>
          timer.sleep(timestamp) >> send(NoteOff(n, channel))
    }.void

  def close: F[Unit] = F.catchNonFatal(device.close())
  def open: F[Unit]  = F.catchNonFatal(device.open())
}
