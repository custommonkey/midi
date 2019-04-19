package interpreters

import algebra.Print
import cats.{Applicative, Show}

import scala.collection.mutable

class StringPrintInterpreter[F[_]: Applicative] extends Print[F] {

  private val out: mutable.Buffer[String] = mutable.Buffer[String]()

  def string: String = out.mkString

  override def apply[T](value: T)(implicit s: Show[T]): F[Unit] =
    Applicative[F].pure(out += s.show(value))

}
