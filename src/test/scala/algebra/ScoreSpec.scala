package algebra

import java.util.concurrent.TimeUnit

import algebra.types.Score
import cats.kernel.laws.discipline.MonoidTests
import cats.kernel.{Eq, Monoid}
import cats.tests.CatsSuite
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

}
