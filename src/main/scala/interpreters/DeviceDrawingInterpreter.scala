package interpreters

import algebra.Device
import algebra.types.{Msg, ToScore}
import cats.Show

class DeviceDrawingInterpreter[F[_]](val device: Device[F]) extends Device[F] {

  override def send[T: Msg: Show](value: T): F[Unit] = ???

  override def bleep[T: ToScore](t: T): F[Unit] = ???

}
