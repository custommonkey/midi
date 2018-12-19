import algebra.Tempo
import eu.timepit.refined.api.Refined
import eu.timepit.refined.auto._
import eu.timepit.refined.numeric.Positive
import eu.timepit.refined.scalacheck.arbitraryRefType
import org.scalacheck.{Gen, ScalacheckShapeless, Arbitrary ⇒ Arb}
import org.scalatest.prop.PropertyChecks
import org.scalatest.{MustMatchers, WordSpec}
import Tempo._

import scala.concurrent.duration._

class BeatSpec extends WordSpec with PropertyChecks with MustMatchers with ScalacheckShapeless {

  implicit def arbInt: Arb[Int Refined Positive]     = arbitraryRefType(Gen.posNum[Int])
  implicit def arbFloat: Arb[Float Refined Positive] = arbitraryRefType(Gen.posNum[Float])

  "beat" should {
    "convert int beats to millis" in {
      forAll { (i: Int, tempo: Tempo) ⇒
        implicit val t = tempo

        val expected = (1.minute / tempo.bpm.toLong) * i.toLong

        i.beats must equal(expected)

      }
    }

    "convert float beats to millis" in {

      forAll { (i: Float, tempo: Tempo) ⇒
        implicit val t = tempo

        val expected = (1.minute / tempo.bpm.toLong) * i.toLong

        i.beats must equal(expected)

      }
    }
  }

}
