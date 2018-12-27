package algebra

import algebra.types.MidiInt
import algebra.types.MidiInt.MidiIntOps
import org.scalatest.prop.PropertyChecks
import org.scalatest.{Matchers, WordSpec}

class MidiIntSpec extends WordSpec with PropertyChecks with Arbitraries with Matchers {

  "MidiInt" should {
    "Not explode" in {
      forAll { (a: MidiInt, b: MidiInt) ⇒
        (a + b).value should be <= 128
      }
    }
  }

}
