package interpreters

import algebra.types.{Channel, Msg}
import algebra.Events.Events
import algebra.Device
import algebra.Event

import scala.concurrent.duration.FiniteDuration

class DeviceDrawingInterpreter[F[_]](val device: Device[F]) extends Device[F] {

  override def apply[T: Msg](value: T): F[Unit] = ???

  override def apply(events: Events[Event[FiniteDuration]])(implicit channel: Channel): F[Unit] =
    ???

}
