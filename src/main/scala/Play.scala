import algebra.Tempo
import algebra.Tempo.BeatOps
import cats.effect.IO
import cats.effect.IO.sleep
import cats.implicits._
import devices.Gervill
import eu.timepit.refined.auto._

import scala.util.Random

object Play extends PlayApp {

  tempo = Tempo(160)

  override def play: IO[Any] =
    devices open Gervill use { device ⇒
      import utils._

      val notes = List.fill(4)(Random.nextInt(44) + 30)
      val bars  = List.fill(8)(notes).flatten

      def boom(i: Int) = device << (i → 1.semiquaver) >> sleep(1.semiquaver)

      val leftHand  = bars.traverse(boom).void
      val rightHand = bars.map(_ + 12).traverse(boom).void

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
