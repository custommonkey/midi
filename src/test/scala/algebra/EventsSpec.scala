package algebra

import algebra.types.Timestamp.TimestampOps
import algebra.types.{Score, Timestamp}
import cats.kernel.laws.discipline.MonoidTests
import cats.kernel.{Eq, Monoid}
import cats.laws.discipline.FunctorTests
import cats.tests.CatsSuite
import eu.timepit.refined.api.RefType
import eu.timepit.refined.auto._
import org.scalacheck.{Cogen, Shrink}
import org.scalatest.FunSuite
import cats.derived.auto.eq._
import eu.timepit.refined.cats.refTypeEq
import algebra.Events._

import scala.concurrent.duration.FiniteDuration

class EventsSpec extends FunSuite with Arbitraries with CatsSuite {

  implicit val shrinkLong: Shrink[Long] = Shrink[Long] { l ⇒
    if (l > 0) {
      Stream(l - 1)
    } else {
      Stream.empty
    }
  }

  implicit val eqDuration: Eq[FiniteDuration]   = (a, b) ⇒ a.toMillis == b.toMillis
  implicit val cogenTimestamp: Cogen[Timestamp] = Cogen[Timestamp]((x: Timestamp) ⇒ x.value)

  implicit val monoid: Monoid[Score] = new Monoid[Score] {
    override def empty: Score                       = List()
    override def combine(x: Score, y: Score): Score = x ::: y
  }

  checkAll("Score.MonoidLaws", MonoidTests[Score].monoid)
  checkAll("Events.MonoidLaws", MonoidTests[Events[Timestamp]].monoid)
  checkAll("Events.FunctorLaws", FunctorTests[Events].functor[Timestamp, Timestamp, Timestamp])
  checkAll("Event.FunctorLaws", FunctorTests[Event].functor[Timestamp, Timestamp, Timestamp])

  test("NoteOrRest => S") {
    forAll { x: NoteOrRest ⇒
      x match {
        case n: Note ⇒
          List(n).events should be(
            Events(
              Event(On, n.n, 0l),
              Event(Off, n.n, timestamp(n))
            ))
        case r: Rest ⇒
          List(r).events should be(Events())
      }
    }
  }

  private def timestamp(x: Long): Timestamp       = RefType.applyRef[Timestamp].unsafeFrom(x)
  private def timestamp(x: NoteOrRest): Timestamp = timestamp(x.duration.toMillis)

  test("Note + Note => S") {
    forAll { (n1: Note, n2: Note) ⇒
      (n1 + n2).events should be(
        Events(
          Event(On, n1.n, 0l),
          Event(Off, n1.n, timestamp(n1)),
          Event(On, n2.n, timestamp(n1)),
          Event(Off, n2.n, timestamp(n1) + timestamp(n2))
        ))
    }
  }

  test("Note + Rest => S") {
    forAll { (note: Note, rest: Rest) ⇒
      (note + rest).events should be(
        Events(
          Event(On, note.n, 0l),
          Event(Off, note.n, timestamp(note))
        ))
    }
  }

  test("Rest + Rest => S") {
    forAll { (r1: Rest, r2: Rest) ⇒
      (r1 + r2).events should be(Events())
    }
  }

  test("Rest + Note => S") {
    forAll { (rest: Rest, note: Note) ⇒
      (rest + note).events should be(
        Events(
          Event(On, note.n, timestamp(rest)),
          Event(Off, note.n, timestamp(rest) + timestamp(note))
        ))
    }
  }

  test("List[Note] => S") {
    forAll { ns: List[Note] ⇒
      val events = ns.events.events
      ns match {
        case Nil ⇒
        case n1 :: _ ⇒
          events.head should be(Event[Timestamp](On, n1.n, 0l))

          val sum = ns.map(_.duration.toMillis).sum
          events.last should be(Event(Off, ns.last.n, timestamp(sum)))
      }
    }
  }

  test("durations") {
    forAll { (n1: List[Note], n2: List[Note]) ⇒
      (n1 ::: n2).events.durations
    }
  }

  test("compose events") {
    forAll { (n1: Event[Timestamp], n2: Event[Timestamp]) ⇒
      (Events(n1) |+| Events(n2)) should be (Events(List(n1, n2).sortBy(_.time.value)))
    }
  }

  test("durations &") {
    forAll { (n1: List[Note], n2: List[Note]) ⇒
      (n1.events & n2.events).durations
    }
  }

}
