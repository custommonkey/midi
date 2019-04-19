package algebra

import algebra.types.Timestamp.TimestampOps
import algebra.types.{Score, Timestamp}
import cats.implicits._
import cats.{Functor, Monoid}
import eu.timepit.refined.auto._

import scala.collection.immutable
import scala.collection.immutable.SortedSet
import scala.concurrent.duration.FiniteDuration

// case class Events[A](events: SortedSet[A]) {
// def &(e: Events[A]): Events[A] = this |+| e
// }

object Events {
  type Events[A] = SortedSet[A]

  implicit def orderingTimestamp(implicit ordering: Ordering[Long]): Ordering[Timestamp] =
    (x: Timestamp, y: Timestamp) => ordering.compare(x.value, y.value)

  implicit def orderingEvent[T](implicit ordering: Ordering[T]): Ordering[Event[T]] =
    (x: Event[T], y: Event[T]) => ordering.compare(x.time, y.time)

  def apply(events: Event[Timestamp]*): Events[Event[Timestamp]] = SortedSet(events: _*)

  implicit def monoidEvents[F[_], A: Ordering]: Monoid[Events[A]] = new Monoid[Events[A]] {
    override def empty: Events[A]                               = SortedSet()
    override def combine(x: Events[A], y: Events[A]): Events[A] = x ++ y
  }

  implicit val functorEvents: Functor[Events] = new Functor[Events] {
    override def map[A, B](fa: Events[A])(f: A => B): Events[B] = ???
  }

  private def nil[T]: (List[Event[T]], Timestamp) = (List(), 0l)

  implicit class EventOps(events: Events[Event[Timestamp]]) {

    def durations: Events[Event[FiniteDuration]] = {
      val xxx: immutable.Seq[Event[FiniteDuration]] = events
        .foldLeft(nil[FiniteDuration]) {
          case ((list, last), e @ Event(_, _, timestamp)) =>
            (list :+ e.as(timestamp - last), timestamp)
        }
        ._1
      SortedSet(xxx: _*)
    }

  }

  implicit class ScoreOps(score: Score) {

    def events: Events[Event[Timestamp]] =
      SortedSet(
        // score
        //   .foldLeft(nil[Timestamp]) {
        //     case ((list, last), Note(n, duration)) ⇒
        //       (list :+ Event(On, n, last) :+ Event(Off, n, last + duration), last + duration)
        //     case ((list, last), Rest(duration)) ⇒
        //       (list, last + duration)
        //   }
        //   ._1
      )

    def +(n: NoteOrRest): Score = score :+ n
  }

}
