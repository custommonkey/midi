package interpreters

import algebra.Device
import algebra.types.{Channel, Msg, ToScore}
import cats.Show

class DeviceDrawingInterpreter[F[_]](val device: Device[F]) extends Device[F] {

  override def send[T: Msg: Show](value: T): F[Unit] = ???

  override def <<[T](t: T)(implicit ch: Channel, s: ToScore[T]): F[Unit] = ???

}
