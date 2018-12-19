package interpreters

import algebra.Print
import cats.{ApplicativeError, Show}

class PrintInterpreter[F[_]](implicit F: ApplicativeError[F, Throwable]) extends Print[F] {

  def apply[T](value: T)(implicit show: Show[T]): F[Unit] =
    F.catchNonFatal(Console.println(show.show(value)))

}
