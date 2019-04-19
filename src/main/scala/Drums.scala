import algebra.Events._
import algebra.Messages.ControlChange._
import algebra.Note
import algebra.Tempo.BeatOps
import algebra.types.{Channel, MidiInt}
import cats.effect.IO
import cats.implicits._
import devices.NordDrum2.ToneDecay
import devices.UnoMidiInterface
import eu.timepit.refined.api.RefType.applyRef
import eu.timepit.refined.auto._

object Drums extends PlayApp {
  val channels: List[Channel] = (0 to 15).map(applyRef[Channel].unsafeFrom(_)).toList

  channel = 10
  val drum: List[MidiInt] = List(36, 37, 38, 44, 45, 46)

  val s = (Note(drum(0), 1.beat) + Note(drum(1), 1.beat)).events

  override def play: IO[Any] =
    devices
      .open(UnoMidiInterface)
      .use { nord =>
        nord(ToneDecay(10)) >> nord.apply(s.durations) >>
          nord(ToneDecay(20)) >> nord(s.durations) >>
          nord(ToneDecay(30)) >> nord(s.durations) >>
          nord(ToneDecay(40)) >> nord(s.durations) >>
          nord(ToneDecay(50)) >> nord(s.durations)
      }
}
