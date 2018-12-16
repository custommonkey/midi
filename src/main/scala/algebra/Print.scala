package algebra
import cats.Show

trait Print[F[_]] {
  def apply[T](value: T)(implicit show: Show[T]): F[Unit]
}
