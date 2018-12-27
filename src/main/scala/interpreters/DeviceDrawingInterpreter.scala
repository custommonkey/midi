package interpreters

import algebra.types.{Channel, Msg}
import algebra.{Device, Events}
import cats.Show

import scala.concurrent.duration.FiniteDuration

class DeviceDrawingInterpreter[F[_]](val device: Device[F]) extends Device[F] {

  override def send[T: Msg: Show](value: T): F[Unit] = ???

  override def <<(events: Events[FiniteDuration])(implicit channel: Channel): F[Unit] = ???

}
