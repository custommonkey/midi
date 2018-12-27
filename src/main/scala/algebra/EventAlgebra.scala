package algebra

import algebra.types.Timestamp
import algebra.types.Timestamp.TimestampOps
import eu.timepit.refined.auto._
import cats.implicits._

import scala.concurrent.duration._

trait EventAlgebra {

  private def nil[T]: (List[Event[T]], Timestamp) = (List(), 0l)

  def score(notes: List[NoteOrRest]): Events[Timestamp] = Events {
    notes
      .foldLeft(nil[Timestamp]) {
        case ((list, last), Note(n, duration)) ⇒
          (list :+ Event(On, n, last) :+ Event(Off, n, last + duration), last + duration)
        case ((list, last), Rest(duration)) ⇒
          (list, last + duration)
      }
      ._1
  }

  def score(n: NoteOrRest*): Events[Timestamp] = score(n.toList)

  def blo(events: Events[Timestamp]): Events[FiniteDuration] =
    Events(
      events.events
        .foldLeft(nil[FiniteDuration]) {
          case ((list, last), e @ Event(_, _, timestamp)) ⇒
            (list :+ e.as(timestamp - last), timestamp)
        }
        ._1
    )

}
