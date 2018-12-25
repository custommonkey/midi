package algebra

import algebra.types.{Score, Timestamp}
import cats.kernel.laws.discipline.MonoidTests
import cats.kernel.{Eq, Monoid}
import cats.tests.CatsSuite
import eu.timepit.refined.api.RefType
import eu.timepit.refined.auto._
import org.scalacheck.Shrink
import org.scalatest.FunSuite
import eu.timepit.refined.cats.refTypeEq
import Timestamp.TimestampOps

import scala.concurrent.duration.FiniteDuration

class ScoreSpec extends FunSuite with Arbitraries with CatsSuite with EventAlgebra {

  implicit val shrinkLong: Shrink[Long] = Shrink[Long] { l ⇒
    if (l > 0) {
      Stream(l - 1)
    } else {
      Stream.empty
    }
  }

  implicit val eqDuration: Eq[FiniteDuration] = (a, b) ⇒ a.toMillis == b.toMillis
  implicit val eqScore: Eq[Score]             = cats.derived.semi.eq
  implicit def eqS[F[_]]: Eq[Events]          = cats.derived.semi.eq

  implicit val monoid: Monoid[Score] = new Monoid[Score] {
    override def empty: Score                       = List()
    override def combine(x: Score, y: Score): Score = x ::: y
  }

  checkAll("Score.MonoidLaws", MonoidTests[Score].monoid)
  checkAll("Events.MonoidLaws", MonoidTests[Events].monoid)

  test("NoteOrRest => S") {
    forAll { x: NoteOrRest ⇒
      x match {
        case n: Note ⇒
          score(n) should be(
            Events(
              Event(On, n.n, 0),
              Event(Off, n.n, timestamp(n))
            ))
        case r: Rest ⇒
          score(r) should be(Events())
      }
    }
  }

  private def timestamp(x: Long): Timestamp       = RefType.applyRef[Timestamp].unsafeFrom(x.toInt)
  private def timestamp(x: NoteOrRest): Timestamp = timestamp(x.duration.toMillis)

  test("Note + Note => S") {
    forAll { (n1: Note, n2: Note) ⇒
      score(n1, n2) should be(
        Events(
          Event(On, n1.n, 0),
          Event(Off, n1.n, timestamp(n1)),
          Event(On, n2.n, timestamp(n1)),
          Event(Off, n2.n, timestamp(n1) + timestamp(n2))
        ))
    }
  }

  test("Note + Rest => S") {
    forAll { (note: Note, rest: Rest) ⇒
      score(note, rest) should be(
        Events(
          Event(On, note.n, 0),
          Event(Off, note.n, timestamp(note))
        ))
    }
  }

  test("Rest + Rest => S") {
    forAll { (r1: Rest, r2: Rest) ⇒
      score(r1, r2) should be(Events())
    }
  }

  test("Rest + Note => S") {
    forAll { (rest: Rest, note: Note) ⇒
      score(rest, note) should be(
        Events(
          Event(On, note.n, timestamp(rest)),
          Event(Off, note.n, timestamp(rest) + timestamp(note))
        ))
    }
  }

  test("List[Note] => S") {
    forAll { ns: List[Note] ⇒
      val events = score(ns).events
      ns match {
        case Nil ⇒
        case n1 :: _ ⇒
          events.head should be(Event(On, n1.n, 0))

          val sum = ns.map(_.duration.toMillis).sum
          events.last should be(Event(Off, ns.last.n, timestamp(sum)))
      }
    }
  }

}
