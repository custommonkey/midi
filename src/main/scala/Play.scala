import algebra.{Note, Tempo}
import algebra.Tempo.BeatOps
import algebra.types.Nint
import cats.effect.IO
import cats.effect.IO.sleep
import cats.implicits._
import devices.Gervill
import eu.timepit.refined.auto._
import Nint.randomNint

object Play extends PlayApp {

  tempo = Tempo(160)

  override def play: IO[Any] =
    devices open Gervill use { device ⇒
      import utils._

      val notes: IO[List[Nint]] = List.fill(4)(random[Nint]).sequence
      val bars: IO[List[Nint]]  = List.fill(8)(notes).sequence.map(_.flatten)

      def boom(i: Nint): IO[Unit] = device << Note(i, 1.semiquaver) >> sleep(1.semiquaver)

      val leftHand  = bars.map(_.traverse(boom).void)
      val rightHand = bars.map(_.map(Nint.+(_, 12)).traverse(boom)).void

      showDevices >>
        showInstruments >>
        device.send(Gervill.AcousticGrandPiano) >>
        (for {
          left  ← leftHand.start
          right ← rightHand.start
          _     ← left.join
          _     ← right.join
        } yield ())
    }
}
