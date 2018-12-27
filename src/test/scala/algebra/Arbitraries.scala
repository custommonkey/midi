package algebra

import java.util.concurrent.TimeUnit.MILLISECONDS

import algebra.types.{MidiInt, Timestamp}
import eu.timepit.refined.scalacheck.arbitraryRefType
import org.scalacheck.{Arbitrary, Gen, ScalacheckShapeless}

import scala.concurrent.duration.FiniteDuration

trait Arbitraries extends ScalacheckShapeless {
  implicit val arbFiniteDuration: Arbitrary[FiniteDuration] = Arbitrary {
    Gen.posNum[Long].map(FiniteDuration(_, MILLISECONDS))
  }
  implicit val arbTimestamp: Arbitrary[Timestamp] = arbitraryRefType(Gen.posNum[Long].map(_ + 1))
  implicit val arbMidiInt: Arbitrary[MidiInt]     = arbitraryRefType(Gen.choose(0, 500))
}
