import algebra.Tempo
import algebra.Tempo._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.auto._
import eu.timepit.refined.numeric.Positive
import eu.timepit.refined.scalacheck.arbitraryRefType
import org.scalacheck.{Gen, ScalacheckShapeless, Arbitrary ⇒ Arb}
import org.scalatest.prop.PropertyChecks
import org.scalatest.{MustMatchers, WordSpec}

import scala.concurrent.duration._

class BeatSpec extends WordSpec with PropertyChecks with MustMatchers with ScalacheckShapeless {

  implicit val arbLong: Arb[Long]                = Arb(Gen.choose(0, 1000))
  implicit val arbFloat: Arb[Float]              = Arb(Gen.posNum[Float])
  implicit val arbInt: Arb[Int Refined Positive] = arbitraryRefType(Gen.choose(0, 1000))

  "beat" should {
    "convert int beats to millis" in
      forAll { (i: Long, tempo: Tempo) ⇒
        implicit val t = tempo

        val expected = (1.minute / tempo.bpm.toLong) * i

        i.beats must equal(expected)

      }

    "convert float beats to millis" in
      forAll { (i: Float, tempo: Tempo) ⇒
        implicit val t = tempo

        val expected = (1.minute / tempo.bpm.toLong) * i.toLong

        i.beats must equal(expected)

      }
  }

}
