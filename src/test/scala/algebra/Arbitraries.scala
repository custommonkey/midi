package algebra

import java.util.concurrent.TimeUnit.MILLISECONDS

import algebra.types.{MidiInt, Timestamp}
import eu.timepit.refined.scalacheck.arbitraryRefType
import javax.sound.midi.MidiDevice.Info
import org.scalacheck.{Arbitrary, Gen, ScalacheckShapeless}
import Arbitrary.arbitrary
import cats.data.NonEmptyChain

import scala.concurrent.duration.FiniteDuration

trait Arbitraries extends ScalacheckShapeless {
  implicit val arbFiniteDuration: Arbitrary[FiniteDuration] = Arbitrary {
    Gen.posNum[Long].map(FiniteDuration(_, MILLISECONDS))
  }
  implicit val arbTimestamp: Arbitrary[Timestamp] = arbitraryRefType(Gen.posNum[Long].map(_ + 1))
  implicit val arbMidiInt: Arbitrary[MidiInt]     = arbitraryRefType(Gen.choose(0, 500))
  implicit val arbInfo: Arbitrary[Info] = Arbitrary {
    for {
      name        ← arbitrary[String]
      vendor      ← arbitrary[String]
      description ← arbitrary[String]
      version     ← arbitrary[String]
    } yield new Info(name, vendor, description, version) {}
  }
  implicit def catsLawsArbitraryForNonEmptyChain[A](
      implicit A: Arbitrary[A]): Arbitrary[NonEmptyChain[A]] =
    Arbitrary(implicitly[Arbitrary[List[A]]].arbitrary.flatMap { chain ⇒
      NonEmptyChain.fromSeq(chain) match {
        case None     ⇒ A.arbitrary.map(NonEmptyChain.one)
        case Some(ne) ⇒ Gen.const(ne)
      }
    })
}
