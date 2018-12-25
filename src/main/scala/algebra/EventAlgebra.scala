package algebra

import algebra.types.{Nint, Timestamp}
import cats.Monoid
import Timestamp.TimestampOps
import eu.timepit.refined.auto._

trait EventAlgebra {

  sealed trait EvType
  case object On  extends EvType
  case object Off extends EvType

  case class Event(e: EvType, n: Nint, t: Timestamp)
  case class Events(events: List[Event])

  object Events {
    def apply(events: Event*): Events = new Events(events.to)
    implicit def monoidEvents[F[_]]: Monoid[Events] = new Monoid[Events] {
      override def empty: Events                         = Events(List())
      override def combine(x: Events, y: Events): Events = Events(x.events ::: y.events)
    }
  }

  private val nil: (List[Event], Timestamp) = (List(), 0)

  def score(notes: List[NoteOrRest]): Events = Events {
    notes
      .foldLeft(nil) {
        case ((list, last), Note(n, duration)) ⇒
          (list :+ Event(On, n, last) :+ Event(Off, n, last + duration), last + duration)
        case ((list, last), Rest(duration)) ⇒
          (list, last + duration)
      }
      ._1
  }

  def score(n: NoteOrRest*): Events = score(n.toList)

}
