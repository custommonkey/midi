package algebra

import algebra.types.Timestamp.TimestampOps
import algebra.types.{Score, Timestamp}
import cats.implicits._
import cats.{Functor, Monoid}
import eu.timepit.refined.auto._

import scala.concurrent.duration.FiniteDuration

case class Events[T](events: List[Event[T]]) {
  def &(e: Events[T]): Events[T] = {
    this |+| e
  }
}

object Events {

  def apply(events: Event[Timestamp]*): Events[Timestamp] = new Events(events.to)

  implicit def monoidEvents[F[_], T]: Monoid[Events[T]] = new Monoid[Events[T]] {
    override def empty: Events[T]                               = Events(List())
    override def combine(x: Events[T], y: Events[T]): Events[T] = Events(x.events ::: y.events)
  }

  implicit val functorEvents: Functor[Events] = new Functor[Events] {
    override def map[A, B](fa: Events[A])(f: A ⇒ B): Events[B] = Events(fa.events.map(_.map(f)))
  }

  private def nil[T]: (List[Event[T]], Timestamp) = (List(), 0l)

  implicit class EventOps(events: Events[Timestamp]) {

    def durations: Events[FiniteDuration] = Events {
      events.events
        .foldLeft(nil[FiniteDuration]) {
          case ((list, last), e @ Event(_, _, timestamp)) ⇒
            (list :+ e.as(timestamp - last), timestamp)
        }
        ._1
    }

  }

  implicit class ScoreOps(score: Score) {

    def events: Events[Timestamp] = Events {
      score
        .foldLeft(nil[Timestamp]) {
          case ((list, last), Note(n, duration)) ⇒
            (list :+ Event(On, n, last) :+ Event(Off, n, last + duration), last + duration)
          case ((list, last), Rest(duration)) ⇒
            (list, last + duration)
        }
        ._1
    }

    def +(n: NoteOrRest): Score = score :+ n
  }

}
