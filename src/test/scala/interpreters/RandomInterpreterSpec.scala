package interpreters

import algebra.types.MidiInt
import algebra.types.MidiInt.randomNint
import cats.implicits._
import org.scalatest.WordSpec
import org.scalatest.prop.PropertyChecks

class RandomInterpreterSpec extends WordSpec with PropertyChecks {

  val random = new RandomInterpreter[List]()

  "RandomInterpreter" should {

    "apply" in {
      0 to 100 foreach { _ ⇒
        random[MidiInt]
      }
    }

  }
}
