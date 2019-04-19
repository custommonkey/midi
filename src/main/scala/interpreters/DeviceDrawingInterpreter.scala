package interpreters

import algebra.types.{Channel, Msg}
import algebra.{Device, Events}

import scala.concurrent.duration.FiniteDuration

class DeviceDrawingInterpreter[F[_]](val device: Device[F]) extends Device[F] {

  override def apply[T: Msg](value: T): F[Unit] = ???

  override def apply(events: Events[FiniteDuration])(implicit channel: Channel): F[Unit] = ???

}
