package algebra

import java.util.concurrent.TimeUnit

import algebra.types.{Note, Score}
import cats.kernel.laws.discipline.MonoidTests
import cats.kernel.{Eq, Monoid}
import cats.tests.CatsSuite
import eu.timepit.refined.api.{RefType, Refined}
import eu.timepit.refined.auto._
import eu.timepit.refined.numeric.NonNegative
import org.scalacheck.{Arbitrary, Gen, ScalacheckShapeless}
import org.scalatest.FunSuite

import scala.concurrent.duration.FiniteDuration

class ScoreSpec extends FunSuite with ScalacheckShapeless with CatsSuite {

  implicit val arbFiniteDuration: Arbitrary[FiniteDuration] = Arbitrary {
    Gen.posNum[Long].map { l ⇒
      FiniteDuration(l, TimeUnit.NANOSECONDS)
    }
  }

  implicit val eqDuration: Eq[FiniteDuration] = (a, b) ⇒ a.toMillis == b.toMillis
  implicit val eqScore: Eq[Score]             = cats.derived.semi.eq

  implicit val monoid: Monoid[Score] = new Monoid[Score] {
    override def empty: Score                       = List()
    override def combine(x: Score, y: Score): Score = x ::: y
  }

  checkAll("Score.MonoidLaws", MonoidTests[Score].monoid)

  type Timestamp = Int Refined NonNegative
  case class N(n: Note, d: FiniteDuration)
  case class Event(n: Note, t: Timestamp)
  case class S(events: List[Event])
  object S {
    def apply(events: Event*): S = new S(events.toList)
  }

  def score(n: List[N]): S = S {
    n.map { n ⇒
      Event(n.n, 0)
    }
  }
  def score(n: N*): S = score(n.toList)

  test("N => S") {
    forAll { n: N ⇒
      score(n) should be(S(Event(n.n, 0)))
    }
  }

  def xxx(x: FiniteDuration) = RefType.applyRef[Timestamp].unsafeFrom(x.toMillis.toInt)

  test("N + N => S") {
    forAll { (n1: N, n2: N) ⇒
      score(n1, n2) should be(S(Event(n1.n, 0), Event(n2.n, xxx(n1.d))))
    }
  }

}
