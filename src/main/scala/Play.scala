import algebra.Tempo.BeatOps
import algebra.types.{Thing, ToScore}
import algebra.{Device, Note, Tempo}
import cats.effect.IO
import cats.effect.IO.sleep
import cats.implicits._
import devices.Gervill
import eu.timepit.refined.auto._

import scala.concurrent.duration.FiniteDuration
import scala.util.Random

object Play extends PlayApp {

  override def play(device: Device[IO]): IO[Any] = {

    import device._
    import utils._

    implicit val tempo = Tempo(160)

    val notes = List.fill(4)(Random.nextInt(44) + 30)
    val bars  = List.fill(8)(notes).flatten

    implicit val tup: ToScore[(Int, FiniteDuration)] = (i: (Int, FiniteDuration)) ⇒
      List(Thing(Note(i._1, i._2)))

    def boom(i: Int) = bleep(i → 1.semiquaver) >> sleep(1.semiquaver)

    val leftHand  = bars.traverse(boom).void
    val rightHand = bars.map(_ + 12).traverse(boom).void

    showInstruments >>
      send(Gervill.AcousticGrandPiano) >>
      (for {
        left  ← leftHand.start
        right ← rightHand.start
        _     ← left.join
        _     ← right.join
      } yield ())
  }
}
