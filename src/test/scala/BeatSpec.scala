import Tempo.Bpm
import eu.timepit.refined.api.Refined
import eu.timepit.refined.auto._
import eu.timepit.refined.numeric.Positive
import eu.timepit.refined.scalacheck.arbitraryRefType
import org.scalacheck.{Gen, Arbitrary ⇒ Arb}
import org.scalatest.prop.PropertyChecks
import org.scalatest.{MustMatchers, WordSpec}

import scala.concurrent.duration._

class BeatSpec extends WordSpec with PropertyChecks with MustMatchers {

  implicit def arbInt: Arb[Int Refined Positive]     = arbitraryRefType(Gen.posNum[Int])
  implicit def arbFloat: Arb[Float Refined Positive] = arbitraryRefType(Gen.posNum[Float])

  "beat" should {
    "convert beats to millis" in {
      forAll { (i: Int , bpm: Bpm) ⇒
        val tempo = new Tempo(bpm)
        import tempo.implicits._

        val expected = (1.minute / bpm.toLong) * i.toLong

        i.beats must equal(expected)

      }

      forAll { (i: Float , bpm: Bpm) ⇒
        val tempo = new Tempo(bpm)
        import tempo.implicits._

        val expected = (1.minute / bpm.toLong) * i.toLong

        i.beats must equal(expected)

      }
    }
  }

}
