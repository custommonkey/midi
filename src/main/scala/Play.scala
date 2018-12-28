import algebra.Tempo.BeatOps
import algebra.types.MidiInt
import algebra.types.MidiInt._
import algebra.{Note, Rest, Tempo}
import cats.effect.IO
import cats.implicits._
import devices.Gervill
import eu.timepit.refined.auto._
import algebra.Events.ScoreOps

object Play extends PlayApp {

  tempo = Tempo(160)

  override def play: IO[Any] =
    devices open Gervill use { device ⇒
      import utils._

      val notes: IO[List[MidiInt]] = List.fill(4)(random[MidiInt]).sequence
      val bars: IO[List[MidiInt]]  = List.fill(8)(notes).sequence.map(_.flatten)

      def boom(i: MidiInt): IO[Unit] =
        device << (Note(i, 1.semiquaver) + Rest(1.semiquaver)).events.durations

      val leftHand  = bars.map(_.traverse(boom).void)
      val rightHand = bars.map(_.map(_ + 12).traverse(boom)).void

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
