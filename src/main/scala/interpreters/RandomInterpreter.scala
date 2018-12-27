package interpreters

import algebra.{Random, RandomApi}
import cats.Applicative

class RandomInterpreter[F[_]](implicit F: Applicative[F]) extends RandomApi[F] {

  def apply[T](implicit random: Random[T]): F[T] = F.pure(random())

}
