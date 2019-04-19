package algebra

import algebra.types.Timestamp.TimestampOps
import algebra.types.{Score, Timestamp}
import cats.implicits._
import cats.{Functor, Monoid}
import eu.timepit.refined.auto._

import scala.collection.immutable
import scala.collection.immutable.SortedSet
import scala.concurrent.duration.FiniteDuration

case class Events(events: SortedSet[Event[Timestamp]]) {
  def &(e: Events): Events = this |+| e
}

object Events {

  implicit def orderingTimestamp(implicit ordering: Ordering[Long]): Ordering[Timestamp] =
    (x: Timestamp, y: Timestamp) ⇒ ordering.compare(x.value, y.value)

  implicit def orderingEvent[T](implicit ordering: Ordering[T]): Ordering[Event[T]] =
    (x: Event[T], y: Event[T]) ⇒ ordering.compare(x.time, y.time)

  def apply(events: Event[Timestamp]*): Events = new Events(SortedSet(events: _*))

  implicit def monoidEvents[F[_], T]: Monoid[Events] = new Monoid[Events] {
    override def empty: Events                               = Events(SortedSet[Event[Timestamp]]())
    override def combine(x: Events, y: Events): Events = Events(x.events ++ y.events)
  }

  implicit val functorEvents: Functor[Events] = new Functor[Events] {
    override def map[A, B](fa: Events)(f: A ⇒ B): Events =
      new Events(SortedSet(fa.events.map(_.map(f))))
  }

  private def nil[T]: (List[Event[T]], Timestamp) = (List(), 0l)

  implicit class EventOps(events: Events) {

    def durations: Events = {
      val xxx: immutable.Seq[Event[FiniteDuration]] = events.events
        .foldLeft(nil[FiniteDuration]) {
          case ((list, last), e @ Event(_, _, timestamp)) ⇒
            (list :+ e.as(timestamp - last), timestamp)
        }
        ._1
      new Events(SortedSet(xxx))
    }

  }

  implicit class ScoreOps(score: Score) {

    def events: Events = new Events(
      SortedSet(
        score
          .foldLeft(nil[Timestamp]) {
            case ((list, last), Note(n, duration)) ⇒
              (list :+ Event(On, n, last) :+ Event(Off, n, last + duration), last + duration)
            case ((list, last), Rest(duration)) ⇒
              (list, last + duration)
          }
          ._1)
    )

    def +(n: NoteOrRest): Score = score :+ n
  }

}
