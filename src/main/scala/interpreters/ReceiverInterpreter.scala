package interpreters

import algebra.types.Msg
import algebra.{Print, Receiver}
import cats.ApplicativeError
import cats.implicits._
import javax.sound.midi
import Shows.showMsg

class ReceiverInterpreter[F[_]](r: midi.Receiver, println: Print[F])(
    implicit F: ApplicativeError[F, Throwable])
    extends Receiver[F] {

  override def apply[T](m: T, i: Long)(implicit msg: Msg[T]): F[Unit] = {
    val message = msg(m)
    println(message) *> F.catchNonFatal(r.send(message, i))
  }

}
